export interface Beneficio {
  id: number;
  nome: string;
  descricao?: string;
  valor: number;
  ativo: boolean;
  versao: number;
}

export interface BeneficioRequest {
  nome: string;
  descricao?: string;
  valor: number;
  ativo: boolean;
}

export interface TransferenciaRequest {
  fromId: number;
  toId: number;
  amount: number;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  details?: string[];
}
