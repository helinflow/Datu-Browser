# DATU Browser — AGENTS.md
## Regras gerais para agentes de IA

Estas regras se aplicam a qualquer agente de IA que trabalhe neste repositório. Leia também as instruções específicas do agente e a documentação da arquitetura antes de alterar o projeto.

## 1. Identidade e prioridades
- Projeto: navegador independente para Android, atualmente em desenvolvimento.
- Linguagem principal observada: Java. Confirme a estrutura real antes de assumir outras tecnologias.
- Preserve a arquitetura e o motor de navegação que estiverem efetivamente implementados.
- Prioridades: segurança, estabilidade, desempenho, compatibilidade e experiência do usuário.
- Não presuma que uma migração para Chromium já foi concluída. Verifique o código e as dependências atuais.

## 2. Inspecione antes de editar
1. Leia os arquivos relevantes e entenda como eles se relacionam.
2. Verifique o estado do Git e as alterações já existentes.
3. Identifique dependências, versões, APIs e requisitos mínimos do Android.
4. Delimite o escopo e os possíveis efeitos colaterais.
5. Se faltar informação essencial, pergunte antes de fazer uma alteração arriscada.

## 3. Proteja o trabalho existente
- Não apague, substitua ou simplifique funcionalidades existentes sem necessidade e justificativa.
- Não sobrescreva alterações do usuário ou de outros colaboradores.
- Não faça mudanças fora do escopo solicitado.
- Prefira alterações pequenas, localizadas e reversíveis.
- Não execute comandos destrutivos, como `git reset --hard`, `git clean -fd`, force push ou reescrita de histórico.
- Não faça commit, push, merge ou publicação sem autorização explícita.
- Nunca inclua senhas, tokens, chaves privadas, keystores ou outros segredos no repositório.

## 4. Código e arquitetura
- Respeite os padrões já utilizados pelo projeto.
- Separe interface, lógica e acesso a dados conforme a arquitetura existente.
- Reutilize componentes e serviços existentes.
- Evite duplicação, abstrações desnecessárias e novas dependências sem justificativa.
- Não invente classes, métodos, bibliotecas ou APIs.
- Trate erros de forma explícita; não os esconda com blocos vazios ou capturas genéricas.
- Mantenha nomes, formatação e organização consistentes com o código próximo.

## 5. Interface e experiência
- Siga o sistema visual e os componentes já adotados pelo DATU.
- Quando a tarefa solicitar Material 3 Expressive, use componentes e padrões compatíveis com as dependências reais do projeto.
- Mantenha hierarquia visual, tipografia, espaçamento, alinhamento, acessibilidade e suporte aos temas existentes.
- Ícones, menus, botões e controles devem ter significado e ação reais.
- Implemente estados de carregamento, vazio, erro e sucesso quando aplicável.
- Não entregue telas vazias, controles decorativos ou animações que prejudiquem desempenho e acessibilidade.
- Não faça redesign fora do escopo pedido.

## 6. Motor de navegação e Chromium
- Primeiro identifique qual motor está realmente integrado e como ele é inicializado.
- Não substitua WebView, Chromium ou outro motor sem solicitação e plano de migração aprovado.
- Não afirme que o projeto usa Chromium se isso não estiver comprovado no código e nas dependências.
- Verifique compatibilidade de versões, APIs, processos, permissões e ciclo de vida.
- Ao reutilizar código do Chromium ou de terceiros, respeite licenças, avisos de copyright e obrigações de distribuição.
- Não copie componentes isolados sem entender suas dependências e contexto.

Referência Chromium Android: https://source.chromium.org/chromium/chromium/src/+/main:chrome/android/

## 7. Segurança e privacidade
- Solicite apenas permissões necessárias e explique seu propósito na interface quando apropriado.
- Valide dados recebidos de URLs, intents, arquivos e conteúdo externo.
- Preserve validação de certificados TLS e proteções contra conteúdo misto conforme a política do navegador.
- Não desative mecanismos de segurança para contornar erros.
- Não introduza telemetria, rastreamento ou compartilhamento de dados sem decisão explícita do responsável pelo projeto.
- Proteja dados locais e não registre informações sensíveis em logs.

## 8. Testes e validação
Após cada alteração relevante:
1. Execute verificações estáticas e testes disponíveis.
2. Compile o módulo afetado quando o ambiente permitir.
3. Verifique recursos, manifest, dependências e configurações Android impactados.
4. Teste fluxos relacionados à mudança.
5. Revise o diff para detectar alterações acidentais.
6. Informe claramente o que não pôde ser testado.

Nunca diga que a compilação ou os testes passaram se não os executou.

## 9. Git e colaboração
- Trabalhe em branch própria para mudanças relevantes.
- Não misture tarefas independentes no mesmo conjunto de alterações.
- Antes de editar, verifique branch e estado do repositório.
- Não altere arquivos que outro agente esteja modificando sem coordenar.
- Prefira pull requests para revisão antes de integrar mudanças à branch principal.

## 10. Relatório obrigatório
Ao concluir, informe:
- objetivo e resumo do que foi alterado;
- arquivos criados ou modificados;
- decisões técnicas importantes;
- testes executados e resultados;
- estado da compilação;
- limitações e pendências conhecidas.

## 11. Regra final
A qualidade e a integridade do DATU são mais importantes que a velocidade. Não improvise, não esconda limitações e não declare uma tarefa concluída sem evidências.
