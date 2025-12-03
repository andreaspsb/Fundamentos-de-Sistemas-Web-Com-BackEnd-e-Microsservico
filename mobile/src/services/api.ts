import axios, { AxiosInstance, AxiosError } from 'axios';
import { storage } from './storage';
import {
  LoginRequest,
  LoginResponse,
  Cliente,
  Pet,
  Categoria,
  Produto,
  Servico,
  Pedido,
  CriarPedido,
  Agendamento,
  CriarAgendamento,
} from '../types';

// URL padrão inicial (será atualizada pelo BackendContext)
const DEFAULT_API_URL = 'http://10.0.2.2:8080/api';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: DEFAULT_API_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Interceptor para adicionar token
    this.api.interceptors.request.use(
      async (config) => {
        const token = await storage.getToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Interceptor para tratar erros
    this.api.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        if (error.response?.status === 401) {
          // Token expirado ou inválido
          await storage.clearAll();
        }
        return Promise.reject(error);
      }
    );
  }

  // Atualiza a URL base (chamado pelo BackendContext)
  setBaseUrl(url: string): void {
    this.api.defaults.baseURL = url;
    console.log(`🔄 API URL atualizada: ${url}`);
  }

  // Retorna a URL base atual
  getBaseUrl(): string {
    return this.api.defaults.baseURL || DEFAULT_API_URL;
  }

  // ============ AUTH ============
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await this.api.post<LoginResponse>('/auth/login', credentials);
    return response.data;
  }

  async register(data: { cliente: Omit<Cliente, 'id'>; username: string; senha: string }): Promise<LoginResponse> {
    const response = await this.api.post<LoginResponse>('/auth/registrar', data);
    return response.data;
  }

  async validateToken(): Promise<boolean> {
    try {
      await this.api.get('/auth/validar');
      return true;
    } catch {
      return false;
    }
  }

  // ============ CLIENTES ============
  async getClientes(): Promise<Cliente[]> {
    const response = await this.api.get<Cliente[]>('/clientes');
    return response.data;
  }

  async getCliente(id: number): Promise<Cliente> {
    const response = await this.api.get<Cliente>(`/clientes/${id}`);
    return response.data;
  }

  async createCliente(cliente: Omit<Cliente, 'id'>): Promise<Cliente> {
    const response = await this.api.post<Cliente>('/clientes', cliente);
    return response.data;
  }

  async updateCliente(id: number, cliente: Cliente): Promise<Cliente> {
    const response = await this.api.put<Cliente>(`/clientes/${id}`, cliente);
    return response.data;
  }

  // ============ PETS ============
  async getPetsByCliente(clienteId: number): Promise<Pet[]> {
    const response = await this.api.get<Pet[]>(`/pets/cliente/${clienteId}`);
    return response.data;
  }

  async createPet(pet: Omit<Pet, 'id'>): Promise<Pet> {
    const response = await this.api.post<Pet>('/pets', pet);
    return response.data;
  }

  async updatePet(id: number, pet: Pet): Promise<Pet> {
    const response = await this.api.put<Pet>(`/pets/${id}`, pet);
    return response.data;
  }

  async deletePet(id: number): Promise<void> {
    await this.api.delete(`/pets/${id}`);
  }

  // ============ CATEGORIAS ============
  async getCategorias(): Promise<Categoria[]> {
    const response = await this.api.get<Categoria[]>('/categorias/ativas');
    return response.data;
  }

  async getCategoria(id: number): Promise<Categoria> {
    const response = await this.api.get<Categoria>(`/categorias/${id}`);
    return response.data;
  }

  // ============ PRODUTOS ============
  async getProdutos(): Promise<Produto[]> {
    const response = await this.api.get<Produto[]>('/produtos/disponiveis');
    return response.data;
  }

  async getProduto(id: number): Promise<Produto> {
    const response = await this.api.get<Produto>(`/produtos/${id}`);
    return response.data;
  }

  async getProdutosByCategoria(categoriaId: number): Promise<Produto[]> {
    const response = await this.api.get<Produto[]>(`/produtos/categoria/${categoriaId}/disponiveis`);
    return response.data;
  }

  async searchProdutos(nome: string): Promise<Produto[]> {
    const response = await this.api.get<Produto[]>('/produtos/buscar', { params: { nome } });
    return response.data;
  }

  // ============ SERVIÇOS ============
  async getServicos(): Promise<Servico[]> {
    const response = await this.api.get<Servico[]>('/servicos/ativos');
    return response.data;
  }

  async getServico(id: number): Promise<Servico> {
    const response = await this.api.get<Servico>(`/servicos/${id}`);
    return response.data;
  }

  // ============ PEDIDOS ============
  async getPedidosByCliente(clienteId: number): Promise<Pedido[]> {
    const response = await this.api.get<Pedido[]>(`/pedidos/cliente/${clienteId}`);
    return response.data;
  }

  async getPedido(id: number): Promise<Pedido> {
    const response = await this.api.get<Pedido>(`/pedidos/${id}`);
    return response.data;
  }

  async createPedido(pedido: CriarPedido): Promise<Pedido> {
    const response = await this.api.post<Pedido>('/pedidos', pedido);
    return response.data;
  }

  async confirmarPedido(id: number): Promise<Pedido> {
    const response = await this.api.post<Pedido>(`/pedidos/${id}/confirmar`);
    return response.data;
  }

  async cancelarPedido(id: number): Promise<Pedido> {
    const response = await this.api.post<Pedido>(`/pedidos/${id}/cancelar`);
    return response.data;
  }

  // ============ AGENDAMENTOS ============
  async getAgendamentosByCliente(clienteId: number): Promise<Agendamento[]> {
    const response = await this.api.get<Agendamento[]>(`/agendamentos/cliente/${clienteId}`);
    return response.data;
  }

  async getAgendamento(id: number): Promise<Agendamento> {
    const response = await this.api.get<Agendamento>(`/agendamentos/${id}`);
    return response.data;
  }

  async checkDisponibilidade(data: string, horario: string): Promise<boolean> {
    const response = await this.api.get<boolean>(`/agendamentos/disponibilidade?data=${data}&horario=${horario}`);
    return response.data;
  }

  async createAgendamento(agendamento: CriarAgendamento): Promise<Agendamento> {
    const response = await this.api.post<Agendamento>('/agendamentos', agendamento);
    return response.data;
  }

  async cancelarAgendamento(id: number): Promise<Agendamento> {
    const response = await this.api.patch<Agendamento>(`/agendamentos/${id}/cancelar`);
    return response.data;
  }
}

export const api = new ApiService();
