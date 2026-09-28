package com.jiralite.tickets.service;

import com.jiralite.tickets.api.dto.ActivityResponse;
import com.jiralite.tickets.api.dto.ChangeStatusRequest;
import com.jiralite.tickets.api.dto.CommentResponse;
import com.jiralite.tickets.api.dto.CreateCommentRequest;
import com.jiralite.tickets.api.dto.CreateTicketRequest;
import com.jiralite.tickets.api.dto.PageResponse;
import com.jiralite.tickets.api.dto.TicketListRequest;
import com.jiralite.tickets.api.dto.TicketResponse;
import com.jiralite.tickets.api.dto.UpdateTicketRequest;
import com.jiralite.tickets.domain.Activity;
import com.jiralite.tickets.domain.Comment;
import com.jiralite.tickets.domain.Ticket;
import com.jiralite.tickets.domain.TicketPriority;
import com.jiralite.tickets.domain.TicketStatus;
import com.jiralite.tickets.domain.UserProduct;
import com.jiralite.tickets.error.ApiException;
import com.jiralite.tickets.error.ErrorCodes;
import com.jiralite.tickets.persistence.ActivityRepository;
import com.jiralite.tickets.persistence.CommentRepository;
import com.jiralite.tickets.persistence.TicketRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TicketServiceImpl implements TicketService {
    private static final Set<TicketStatus> EDITABLE =
            Set.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.REOPEN);

    private final TicketRepository tickets;
    private final CommentRepository comments;
    private final ActivityRepository activities;
    private final MongoTemplate mongo;
    private final MembershipService memberships;
    private final TicketValidation validation;
    private final TicketStateMachine machine;
    private final ActivityRecorder activityRecorder;

    public TicketServiceImpl(
            TicketRepository tickets,
            CommentRepository comments,
            ActivityRepository activities,
            MongoTemplate mongo,
            MembershipService memberships,
            TicketValidation validation,
            TicketStateMachine machine,
            ActivityRecorder activityRecorder) {
        this.tickets = tickets;
        this.comments = comments;
        this.activities = activities;
        this.mongo = mongo;
        this.memberships = memberships;
        this.validation = validation;
        this.machine = machine;
        this.activityRecorder = activityRecorder;
    }

    @Override
    public TicketResponse create(UUID actorId, CreateTicketRequest request) {
        validation.requireTitle(request.getTitle());
        validation.requireDescription(request.getDescription());
        TicketPriority priority = validation.parsePriority(request.getPriority(), TicketPriority.MEDIUM);
        List<UserProduct> actorMemberships = memberships.membershipsOf(actorId);
        if (actorMemberships.isEmpty()) {
            throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        }
        UUID productId = resolveProduct(actorId, request.getProductId(), actorMemberships);
        if (!memberships.isMember(actorId, productId)) {
            throw new ApiException(ErrorCodes.PRODUCT_ACCESS_DENIED, HttpStatus.FORBIDDEN);
        }
        UUID assignee = request.getAssigneeId() == null ? actorId : request.getAssigneeId();
        if (!memberships.isMember(assignee, productId)) {
            throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        }
        Instant now = Instant.now();
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID().toString());
        ticket.setProductId(productId);
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(priority);
        ticket.setReporterId(actorId);
        ticket.setAssigneeId(assignee);
        ticket.setVersion(1);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        return toResponse(tickets.save(ticket));
    }

    @Override
    public PageResponse<TicketResponse> list(UUID actorId, TicketListRequest request) {
        Set<UUID> memberProducts =
                memberships.membershipsOf(actorId).stream()
                        .map(m -> m.getId().getProductId())
                        .collect(Collectors.toSet());
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 20 : request.getSize();
        if (memberProducts.isEmpty()) {
            return new PageResponse<>(List.of(), page, size, 0, 0);
        }
        Criteria criteria = Criteria.where("productId").in(memberProducts);
        List<Criteria> and = new ArrayList<>();
        and.add(criteria);
        if (request.getProduct() != null && !request.getProduct().isEmpty()) {
            List<UUID> allowed =
                    request.getProduct().stream().filter(memberProducts::contains).toList();
            and.add(Criteria.where("productId").in(allowed));
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            List<TicketStatus> statuses =
                    request.getStatus().stream().map(validation::parseStatus).toList();
            and.add(Criteria.where("status").in(statuses));
        }
        if (request.getAssignee() != null && !request.getAssignee().isEmpty()) {
            and.add(Criteria.where("assigneeId").in(request.getAssignee()));
        }
        if (request.getReporter() != null && !request.getReporter().isEmpty()) {
            and.add(Criteria.where("reporterId").in(request.getReporter()));
        }
        if (request.getUser() != null && !request.getUser().isEmpty()) {
            and.add(
                    new Criteria()
                            .orOperator(
                                    Criteria.where("reporterId").in(request.getUser()),
                                    Criteria.where("assigneeId").in(request.getUser())));
        }
        String searchKey = request.getSearchKey();
        if (searchKey != null && !searchKey.isEmpty()) {
            String pattern = Pattern.quote(searchKey);
            and.add(
                    new Criteria()
                            .orOperator(
                                    Criteria.where("title").regex(pattern, "i"),
                                    Criteria.where("description").regex(pattern, "i")));
        }
        Criteria combined = new Criteria().andOperator(and.toArray(Criteria[]::new));
        Query countQuery = new Query(combined);
        long total = mongo.count(countQuery, Ticket.class);
        Query pageQuery =
                new Query(combined)
                        .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                        .skip((long) page * size)
                        .limit(size);
        List<TicketResponse> content = mongo.find(pageQuery, Ticket.class).stream().map(this::toResponse).toList();
        int totalPages = size == 0 ? 0 : (int) Math.ceil(total / (double) size);
        return new PageResponse<>(content, page, size, total, totalPages);
    }

    @Override
    public TicketResponse get(UUID actorId, String id) {
        return toResponse(loadVisible(actorId, id));
    }

    @Override
    public TicketResponse patch(UUID actorId, String id, UpdateTicketRequest request) {
        if (request.isStatusPresent()) {
            throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        }
        Ticket ticket = loadVisible(actorId, id);
        if (!EDITABLE.contains(ticket.getStatus())) {
            throw new ApiException(ErrorCodes.TICKET_FROZEN, HttpStatus.CONFLICT);
        }
        if (request.getVersion() == null || request.getVersion() != ticket.getVersion()) {
            throw new ApiException(ErrorCodes.STALE_VERSION, HttpStatus.CONFLICT);
        }
        Map<String, Object> from = new LinkedHashMap<>();
        Map<String, Object> to = new LinkedHashMap<>();
        if (request.isTitlePresent()) {
            validation.requireTitle(request.getTitle());
            if (!request.getTitle().equals(ticket.getTitle())) {
                from.put("title", ticket.getTitle());
                to.put("title", request.getTitle());
                ticket.setTitle(request.getTitle());
            }
        }
        if (request.isDescriptionPresent()) {
            validation.requireDescription(request.getDescription());
            String next = request.getDescription();
            String prev = ticket.getDescription();
            if (next == null ? prev != null : !next.equals(prev)) {
                from.put("description", prev);
                to.put("description", next);
                ticket.setDescription(next);
            }
        }
        if (request.isPriorityPresent()) {
            TicketPriority priority = validation.parsePriority(request.getPriority(), null);
            if (priority == null) {
                throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
            }
            if (priority != ticket.getPriority()) {
                from.put("priority", ticket.getPriority().name());
                to.put("priority", priority.name());
                ticket.setPriority(priority);
            }
        }
        if (request.isAssigneeIdPresent()) {
            if (request.getAssigneeId() == null) {
                throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
            }
            if (!memberships.isMember(request.getAssigneeId(), ticket.getProductId())) {
                throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
            }
            if (!request.getAssigneeId().equals(ticket.getAssigneeId())) {
                from.put("assigneeId", ticket.getAssigneeId().toString());
                to.put("assigneeId", request.getAssigneeId().toString());
                ticket.setAssigneeId(request.getAssigneeId());
            }
        }
        ticket.setVersion(ticket.getVersion() + 1);
        ticket.setUpdatedAt(Instant.now());
        Ticket saved = tickets.save(ticket);
        activityRecorder.recordFields(saved, actorId, from, to);
        return toResponse(saved);
    }

    @Override
    public TicketResponse changeStatus(UUID actorId, String id, ChangeStatusRequest request) {
        Ticket ticket = loadVisible(actorId, id);
        if (request.version() != ticket.getVersion()) {
            throw new ApiException(ErrorCodes.STALE_VERSION, HttpStatus.CONFLICT);
        }
        TicketStatus target = validation.parseStatus(request.status());
        machine.requireAllowed(ticket.getStatus(), target);
        TicketStatus previous = ticket.getStatus();
        ticket.setStatus(target);
        ticket.setVersion(ticket.getVersion() + 1);
        ticket.setUpdatedAt(Instant.now());
        Ticket saved = tickets.save(ticket);
        activityRecorder.recordStatus(saved, actorId, previous, target);
        return toResponse(saved);
    }

    @Override
    public List<CommentResponse> comments(UUID actorId, String id) {
        loadVisible(actorId, id);
        return comments.findByTicketIdOrderByCreatedAtAsc(id).stream().map(this::toComment).toList();
    }

    @Override
    public CommentResponse addComment(UUID actorId, String id, CreateCommentRequest request) {
        Ticket ticket = loadVisible(actorId, id);
        if (!EDITABLE.contains(ticket.getStatus())) {
            throw new ApiException(ErrorCodes.TICKET_FROZEN, HttpStatus.CONFLICT);
        }
        validation.requireComment(request.body());
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID().toString());
        comment.setTicketId(id);
        comment.setAuthorId(actorId);
        comment.setBody(request.body());
        comment.setCreatedAt(Instant.now());
        return toComment(comments.save(comment));
    }

    @Override
    public List<ActivityResponse> activity(UUID actorId, String id) {
        loadVisible(actorId, id);
        return activities.findByTicketIdOrderByAtAsc(id).stream().map(this::toActivity).toList();
    }

    private Ticket loadVisible(UUID actorId, String id) {
        Ticket ticket =
                tickets.findById(id)
                        .orElseThrow(() -> new ApiException(ErrorCodes.TICKET_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (!memberships.isMember(actorId, ticket.getProductId())) {
            throw new ApiException(ErrorCodes.PRODUCT_ACCESS_DENIED, HttpStatus.FORBIDDEN);
        }
        return ticket;
    }

    private UUID resolveProduct(UUID actorId, UUID requested, List<UserProduct> actorMemberships) {
        if (actorMemberships.size() == 1) {
            return actorMemberships.getFirst().getId().getProductId();
        }
        if (requested == null) {
            throw new ApiException(ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        }
        boolean allowed =
                actorMemberships.stream().anyMatch(m -> m.getId().getProductId().equals(requested));
        if (!allowed) {
            throw new ApiException(ErrorCodes.PRODUCT_ACCESS_DENIED, HttpStatus.FORBIDDEN);
        }
        return requested;
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getProductId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus().name(),
                ticket.getPriority().name(),
                ticket.getReporterId(),
                ticket.getAssigneeId(),
                ticket.getVersion(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }

    private CommentResponse toComment(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicketId(),
                comment.getAuthorId(),
                comment.getBody(),
                comment.getCreatedAt());
    }

    private ActivityResponse toActivity(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getTicketId(),
                activity.getActorId(),
                activity.getAt(),
                activity.getField(),
                activity.getFrom(),
                activity.getTo());
    }
}
