export interface ManagedServer {
  id: string | null;
  name: string;
  url: string;
  type: 'INTERNAL' | 'REMOTE';
}

export interface ProxyStatus {
  enabled: boolean;
  targetUrl: string;
  record: boolean;
  wiremockPort: number;
}

export interface StubMapping {
  id: string;
  request: {
    method?: string;
    url?: string;
    urlPattern?: string;
    urlPath?: string;
  };
  response: {
    status: number;
    jsonBody?: unknown;
    headers?: Record<string, string>;
  };
  priority?: number;
}

export interface RecordingStatus {
  status: 'Recording' | 'NeverStarted' | 'Stopped';
}
