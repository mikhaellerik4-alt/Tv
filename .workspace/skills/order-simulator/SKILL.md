---
name: order-simulator
description: Simula fluxos de pedidos E2E para sistemas de restaurante, validando a integração entre cliente, atendente, cozinha e entregador.
---

# Order Simulator Skill

Habilidade especializada em simular fluxos completos de pedidos para sistemas de restaurante, validando a integração entre cliente, atendente, cozinha e entregador.

## Quando usar
Use esta habilidade quando o usuário solicitar testes, demonstrações ou validações de ponta a ponta (E2E) do sistema de pedidos. Ela é ideal para verificar se as notificações estão chegando, se os status mudam corretamente e se a interface reflete as atualizações em tempo real.

## Capacidades
1. **Simulação de Cliente**: Cria pedidos via interface de delivery ou API, preenchendo dados de entrega e itens do carrinho.
2. **Fluxo de Atendimento**: Simula a aceitação do pedido pelo garçom/atendente.
3. **Fluxo de Cozinha**: Simula o início da preparação e a conclusão do prato.
4. **Fluxo de Logística**: Simula a saída para entrega e a finalização (entrega concluída).
5. **Validação de Notificações**: Verifica se alertas sonoros e visuais (como o pulse de "Novo Pedido") são disparados.

## Guia de Uso
- **Ativação**: "/skill:order-simulator" ou peça para "simular um pedido completo".
- **Parâmetros**: Você pode especificar o tipo de produto (ex: "simule um pedido de hambúrguer") ou o método de pagamento.
- **Verificação**: A skill irá navegar entre os painéis (usando `browser--navigate_to_sandbox`) para demonstrar o progresso em tempo real.

## Exemplos de Prompts
- "Teste todo o sistema simulando um pedido vindo de um cliente online."
- "Verifique se a cozinha recebe a notificação quando o atendente aceita o pedido."
- "Simule um pedido de PDV com geração de senha."
