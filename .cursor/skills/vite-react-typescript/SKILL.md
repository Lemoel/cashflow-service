---
name: vite-react-typescript
description: Guides frontend development with Vite, React, and TypeScript. Use when building or maintaining React apps with Vite, writing TypeScript components and hooks, configuring Vite/TS, or when the user mentions Vite, React, or TypeScript frontend.
---

# Vite + React + TypeScript

## Configuração

### Vite

- Use `@vitejs/plugin-react` ou `@vitejs/plugin-react-swc` para React.
- Alias de path recomendado: `"@": path.resolve(__dirname, "./src")` em `vite.config` e `"@/*": ["./src/*"]` no `tsconfig`.

```ts
// vite.config.ts
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { "@": path.resolve(__dirname, "./src") },
  },
});
```

### TypeScript

- `moduleResolution: "bundler"`, `"jsx": "react-jsx"`, `noEmit: true` para uso com Vite.
- Mantenha `paths` alinhado ao alias do Vite para imports `@/...`.

## Componentes

- Preferir **function components** e **TypeScript** para props.

```tsx
// Props tipadas com interface
interface ButtonProps {
  label: string;
  onClick: () => void;
  disabled?: boolean;
}

export function Button({ label, onClick, disabled = false }: ButtonProps) {
  return (
    <button type="button" onClick={onClick} disabled={disabled}>
      {label}
    </button>
  );
}
```

- Para componentes que aceitam filhos: `React.ReactNode` ou `props: { children?: React.ReactNode }`.
- Exportar o tipo das props quando for reutilizado (ex.: `export type { ButtonProps }`).

## Hooks e estado

- Preferir `useState`, `useReducer` ou bibliotecas de estado (ex.: React Query para servidor) em vez de estado global desnecessário.
- Tipar o estado: `useState<MeuTipo>(valorInicial)`.
- Custom hooks: retorno tipado e dependências estáveis (useCallback/useMemo quando fizer sentido).

```ts
function useCounter(initial = 0): [number, () => void] {
  const [count, setCount] = useState(initial);
  const increment = useCallback(() => setCount((c) => c + 1), []);
  return [count, increment];
}
```

## Roteamento

- Com React Router: usar `createBrowserRouter` + `RouterProvider` (React Router 6) ou `BrowserRouter` + `Routes`/`Route`.
- Tipar parâmetros de rota com `useParams<{ id: string }>()` quando necessário.

## Imports

- Usar alias `@` para código em `src`: `import { X } from "@/components/X"`.
- Evitar caminhos relativos longos (`../../../`); preferir `@/`.

## Testes (Vitest + Testing Library)

- Vitest como runner: `vitest` no `package.json`, tipos em `tsconfig`: `"types": ["vitest/globals"]`.
- Testes de componentes: `@testing-library/react` + `@testing-library/jest-dom`.

```tsx
import { render, screen, userEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { Button } from "@/components/Button";

describe("Button", () => {
  it("calls onClick when clicked", async () => {
    const onClick = vi.fn();
    render(<Button label="Ok" onClick={onClick} />);
    await userEvent.click(screen.getByRole("button", { name: /ok/i }));
    expect(onClick).toHaveBeenCalledTimes(1);
  });
});
```

- Preferir queries acessíveis (`getByRole`, `getByLabelText`) e `userEvent` para interações.

## Build e scripts

- Desenvolvimento: `vite` ou `vite build --watch` conforme necessidade.
- Build de produção: `vite build`; saída em `dist` por padrão.
- Preview: `vite preview` para testar o build.

## Boas práticas

- Manter componentes focados; extrair lógica para hooks ou utils em `@/lib`/`@/utils`.
- Evitar `any`; usar `unknown` e type guards quando o tipo for incerto.
- Nomes em inglês para código (componentes, funções, arquivos).
- CSS: Tailwind ou CSS Modules são comuns; manter convenção do projeto (ex.: classes em inglês).

## Resumo rápido

| Tarefa              | Ação |
|---------------------|------|
| Novo componente     | Function component + interface de props em TS/TSX |
| Import de `src`     | Alias `@/` (ex.: `@/components/...`) |
| Estado local        | `useState<T>` ou hook customizado tipado |
| Teste de componente | Vitest + `render`/`screen`/`userEvent` + queries acessíveis |
| Config Vite         | Plugin React (ou SWC), alias `@` → `./src` |
