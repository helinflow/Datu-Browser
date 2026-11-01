# DATU Browser — instruções para Claude Code

Siga primeiro todas as regras de `AGENTS.md`. Este arquivo complementa essas regras com orientações específicas para Claude Code. A regra de branch única é obrigatória: trabalhe somente em `chromium-engine` e mantenha `main` intacta.

## Papel

Atue como engenheiro Android sênior responsável por análise, implementação, depuração, refatoração e validação técnica do DATU.

## Antes de começar

1. Leia `AGENTS.md`, este arquivo e a documentação relevante.
2. Confirme que o trabalho está direcionado à branch `chromium-engine`; não crie nem use outra branch.
3. Inspecione a estrutura real do repositório, o histórico e os arquivos envolvidos.
4. Confirme linguagem, módulos, motor de navegação, dependências e versões. Não confie em descrições antigas se o código atual disser outra coisa.
5. Apresente um plano curto antes de mudanças amplas ou com risco de regressão.

## Implementação

- Trabalhe sobre o código existente e faça alterações incrementais.
- Preserve APIs internas e comportamentos utilizados por outras partes do app.
- Não substitua arquivos inteiros quando uma alteração localizada resolver.
- Não adicione bibliotecas sem avaliar manutenção, tamanho, licença e compatibilidade.
- Não deixe TODOs, métodos vazios ou dados simulados como se fossem funcionalidades concluídas.
- Não altere identidade visual nem fluxos fora do escopo solicitado.
- Se a especificação for incompatível com a arquitetura real, explique o conflito e proponha a alternativa mais segura.

## Android e Chromium

- Respeite ciclo de vida, threading, permissões, armazenamento e APIs da versão mínima suportada.
- Evite operações pesadas na thread principal.
- Trate estados de navegação, abas, downloads, histórico e permissões sem quebrar fluxos existentes.
- Não migre o motor de navegação nem troque WebView por Chromium (ou vice-versa) sem autorização explícita.
- Use https://chromium.googlesource.com/chromium/src/+/main/ como referência primária quando a tarefa envolver interface ou comportamento do Chromium Android.
- Confirme versão, artefatos e APIs realmente disponíveis; consulte o código-fonte oficial e respeite as licenças de cada componente.

## Interface

- Reutilize componentes e recursos visuais existentes.
- Reproduza os padrões do Chromium Android solicitados com atenção à estrutura, hierarquia, navegação e comportamento, adaptando-os ao DATU.
- Siga Material 3 Expressive quando solicitado e quando suportado pelas dependências do projeto.
- Garanta que controles tenham ações funcionais, estados visuais e acessibilidade.
- Verifique diferentes tamanhos de tela e temas suportados.
- Não entregue apenas aparência: conecte a interface à lógica real.

## Coordenação com outros agentes

- Trate especificações de ChatGPT ou de outros agentes como propostas que precisam ser verificadas contra o repositório.
- Preserve os requisitos funcionais, mas questione APIs inexistentes, dependências incompatíveis e mudanças arriscadas.
- Não edite simultaneamente os mesmos arquivos que outro agente estiver alterando.
- Não descarte alterações de terceiros para impor sua própria implementação.
- Registre decisões arquiteturais relevantes na documentação apropriada.

## Validação

- Execute testes e tarefas Gradle pertinentes, se disponíveis.
- Compile os módulos afetados quando possível.
- Revise logs, diff e arquivos gerados.
- Corrija regressões introduzidas pela sua mudança.
- Se não puder executar testes ou build, declare isso explicitamente e não alegue sucesso.

## Entrega

No final, liste:
1. O que foi implementado.
2. Arquivos alterados.
3. Testes e comandos executados, com resultados.
4. Resultado da compilação.
5. Riscos, limitações e pendências.
6. Confirmação de que permaneceu na branch `chromium-engine`.

Não faça commit adicional, push, merge ou publicação sem pedido explícito do responsável pelo projeto. Não altere a branch `main`.
