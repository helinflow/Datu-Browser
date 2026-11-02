# DATU Browser — AGENTS.md

## Escopo obrigatório de trabalho

- A branch de trabalho deste projeto é exclusivamente `chromium-engine`.
- Toda análise, edição e implementação solicitada deve ser feita nessa branch.
- Não criar, trocar para ou trabalhar em branches paralelas para estas tarefas.
- Não alterar `main`; ela deve permanecer intacta.
- Não integrar, fazer merge ou abrir alterações direcionadas à `main` sem autorização explícita.
- Antes de qualquer alteração, confirme que o alvo é `chromium-engine` e revise o estado atual dos arquivos e commits.
- Se a branch não estiver disponível ou houver conflito que impeça o trabalho seguro, pare e informe; não improvise outra branch.

## 1. Identidade e prioridades

- Projeto: DATU, navegador independente para Android.
- Linguagem e arquitetura devem ser confirmadas no código existente; não presuma migrações ou tecnologias não verificadas.
- Prioridades: segurança, estabilidade, desempenho, compatibilidade, acessibilidade e experiência do usuário.
- Preserve o comportamento existente e implemente mudanças de forma incremental.

## 2. Inspecione antes de editar

1. Leia os arquivos relevantes e entenda suas relações.
2. Verifique branch, histórico recente e alterações existentes.
3. Confirme módulos, linguagem, dependências, versões, APIs e requisitos mínimos do Android.
4. Identifique impactos em navegação, abas, histórico, favoritos, downloads, permissões e ciclo de vida.
5. Delimite o escopo e os riscos antes de mudanças amplas.
6. Se faltar informação essencial, pergunte antes de uma alteração arriscada.

## 3. Proteja o trabalho existente

- Não apague, substitua ou simplifique funcionalidades sem necessidade demonstrável.
- Não sobrescreva alterações do usuário ou de outros colaboradores.
- Não faça mudanças fora do escopo solicitado.
- Prefira alterações pequenas, localizadas e reversíveis.
- Não execute comandos destrutivos, como `git reset --hard`, `git clean -fd`, force push ou reescrita de histórico.
- Não faça commit, push, merge, publicação ou release sem autorização explícita.
- Nunca inclua senhas, tokens, chaves privadas, keystores ou outros segredos no repositório.

## 4. Código e arquitetura

- Respeite os padrões já utilizados pelo projeto.
- Reutilize componentes, serviços e abstrações existentes quando apropriado.
- Separe interface, lógica e acesso a dados conforme a arquitetura real.
- Evite duplicação, abstrações desnecessárias e novas dependências sem justificativa.
- Não invente classes, métodos, bibliotecas ou APIs.
- Trate erros explicitamente; não os esconda com blocos vazios ou capturas genéricas.
- Mantenha nomes, formatação e organização consistentes com o código próximo.
- Não deixe TODOs, métodos vazios ou dados simulados apresentados como funcionalidades concluídas.

## 5. Chromium Android e motor de navegação

- A referência principal para interface e padrões Android do Chromium é https://source.chromium.org/chromium/chromium/src/+/main:chrome/android/
- Consulte o código-fonte real antes de reproduzir comportamentos, componentes ou fluxos.
- Reutilize/adapte implementações de forma consciente: identifique dependências, recursos, APIs, ciclo de vida e serviços associados; não copie trechos isolados sem seu contexto.
- Confirme qual motor está realmente integrado, sua versão, artefatos e APIs disponíveis.
- Não declare que o DATU usa o motor Chromium se isso não estiver comprovado nas dependências e no código.
- Não troque WebView, Chromium ou outro motor sem solicitação explícita e avaliação técnica.
- Preserve a integração entre a interface e as funções reais do navegador.
- Respeite licenças, avisos de copyright, atribuições e obrigações de distribuição de Chromium e de cada dependência. Não presuma que o código Chromium está sob MIT; verifique a licença de cada componente.
- Não copie marcas, recursos proprietários ou ativos sem verificar os direitos e requisitos aplicáveis.

## 6. Interface e experiência do usuário

- Use a implementação Android do Chromium como referência visual e funcional quando isso fizer parte do pedido.
- Priorize fidelidade estrutural ao padrão solicitado, adaptando-o à arquitetura e identidade do DATU sem inventar telas ou fluxos desnecessários.
- Siga a identidade Datu Minimal: superfícies pretas/grafite, tipografia clara, bordas discretas e componentes AppCompat/nativos. Não adicione Material 3 Expressive ou Material Components sem solicitação explícita.
- Mantenha hierarquia visual, tipografia, espaçamento, alinhamento, acessibilidade e suporte aos temas existentes.
- Ícones, menus, botões e controles devem ter significado e ações reais.
- Implemente estados de carregamento, vazio, erro e sucesso quando aplicável.
- Não entregue telas vazias, controles decorativos ou painéis flutuantes quando o fluxo solicitado exigir telas próprias.
- Não faça redesign fora do escopo pedido.
- Verifique tamanhos de tela, orientação e temas suportados.

## 7. Segurança e privacidade

- Solicite apenas permissões necessárias e explique seu propósito quando apropriado.
- Valide dados recebidos de URLs, intents, arquivos e conteúdo externo.
- Preserve validação de certificados TLS e proteções contra conteúdo misto conforme a política do navegador.
- Não desative mecanismos de segurança para contornar erros.
- Não introduza telemetria, rastreamento ou compartilhamento de dados sem decisão explícita do responsável.
- Proteja dados locais e não registre informações sensíveis em logs.
- Trate downloads, uploads, navegação externa e permissões com validação adequada.

## 8. Testes e validação

Após cada alteração relevante:
1. Execute verificações estáticas e testes disponíveis.
2. Compile o módulo afetado quando o ambiente permitir.
3. Verifique recursos, manifest, dependências e configurações Android impactados.
4. Teste os fluxos relacionados à mudança.
5. Revise o diff para detectar alterações acidentais.
6. Informe claramente o que não pôde ser testado.

Nunca diga que a compilação ou os testes passaram se não os executou.

## 9. Coordenação entre agentes

- Trate especificações produzidas por ChatGPT, Claude ou outros agentes como propostas que devem ser verificadas contra o repositório.
- Preserve requisitos funcionais válidos, mas questione APIs inexistentes, dependências incompatíveis e mudanças arriscadas.
- Não edite simultaneamente os mesmos arquivos que outro agente estiver alterando.
- Não descarte alterações de terceiros para impor sua própria implementação.
- Registre decisões arquiteturais relevantes na documentação apropriada.
- Todos os agentes devem respeitar a regra de branch única `chromium-engine`.

## 10. Relatório obrigatório

Ao concluir uma tarefa, informe:
- objetivo e resumo do que foi alterado;
- arquivos criados ou modificados;
- decisões técnicas importantes;
- testes e comandos executados, com resultados;
- estado da compilação;
- limitações e pendências conhecidas;
- confirmação de que o trabalho permaneceu em `chromium-engine`.

## Regra final

A integridade do DATU e da branch `chromium-engine` é mais importante que a velocidade. Não improvise, não esconda limitações e não declare uma tarefa concluída sem evidências.
