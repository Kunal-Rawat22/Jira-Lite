const TOKEN = "jwt";

export function saveToken(token: string) {
  sessionStorage.setItem(TOKEN, token);
}

export function clearToken() {
  sessionStorage.removeItem(TOKEN);
}

export function getToken() {
  return sessionStorage.getItem(TOKEN);
}

export function isSignedIn() {
  return Boolean(getToken());
}
