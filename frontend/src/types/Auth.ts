export interface LoginRequest {
  emailOrUsername: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  isCreator: boolean;
  isOwner: boolean;
}

export interface JwtResponse {
  token: string;
  userId: number;
  email: string;
  username: string;
  roles: string[];
}

export interface User {
  userId: number;
  email: string;
  username: string;
  roles: string[];
}

