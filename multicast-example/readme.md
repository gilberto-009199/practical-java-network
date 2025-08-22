
## Capitulo 13 - IP Multicast

>EU, terminei de ler agor ae spo resumir comgpt e fazer os codigos baseados na teoria.

Os sockets dos capítulos anteriores são **unicast**, ou seja, fornecem comunicação **ponto a ponto**. Eles estabelecem uma conexão entre dois pontos bem definidos: um remetente e um destinatário. Embora possam trocar de papéis, em qualquer momento é fácil identificar quem é quem.

No entanto, embora a comunicação ponto a ponto atenda a muitas necessidades (assim como conversas individuais atendem há milênios), algumas tarefas exigem um modelo diferente.

---

###### **Broadcasting: O Modelo de Transmissão**

Um exemplo clássico é uma **estação de TV**, que transmite dados de um local para todos os pontos dentro do alcance de seu sinal:
- O sinal chega a todas as TVs, estejam ligadas ou não.
- Atinge até casas sem TV ou com cabos em vez de antenas.

**Características do Broadcasting:**  
✅ **Indiscriminado**: Todos recebem, independentemente de interesse.  
⚠ **Ineficiente**: Consome largura de banda e energia desnecessariamente.

---

###### **Multicasting: Comunicação Seletiva**

Em contraste, o **multicast** envia dados para um **grupo seleto**. Exemplos:
1. **Videoconferência**: Transmite áudio/vídeo apenas para participantes.
2. **Atualizações de roteadores DNS**: Propagadas para vários roteadores.
3. **Usenet (fóruns antigos)**: Uma postagem é replicada para milhares de servidores.

**Como Funciona?**
- O remetente **não envia individualmente** para cada destinatário.
- Roteadores intermediários **replicam e encaminham** as mensagens de forma eficiente.
- Reduz o tráfego na rede, pois evita envios redundantes.

---

###### **Multicast na Internet**

- **Baseado em UDP**: Usa `DatagramPacket` + `MulticastSocket` (uma extensão do `DatagramSocket`).
- **Roteadores inteligentes**: Enviam **uma única cópia** da mensagem até um ponto próximo aos destinatários, que então a replicam localmente.

**Vantagens:**  
✅ **Eficiência**: Minimiza o tráfego na rede.  
✅ **Escalabilidade**: Funciona bem para grupos grandes.

---

###### **Comparação Resumida**

| **Tipo**       | **Exemplo**          | **Eficiência** | **Escopo**            |  
|----------------|----------------------|----------------|-----------------------|  
| **Unicast**    | Conexão TCP, chamada VoIP | Alta (1:1)     | Dois endpoints        |  
| **Broadcast**  | Sinal de TV aberta   | Baixa          | Todos no alcance      |  
| **Multicast**  | Videoconferência, DNS | Alta (1:N)     | Grupo selecionado     |  

---

###### **Implementação em Java**

- **Pacote usado**: `java.net.MulticastSocket` (estende `DatagramSocket`).
- **Protocolo subjacente**: UDP com roteamento especial (IGMP).

**Exemplo de uso:**

```java
MulticastSocket socket = new MulticastSocket(porta);
socket.joinGroup(enderecoMulticast); // Entra em um grupo
// Envia/recebe DatagramPacket como em UDP comum
```

---

###### **Por Que Usar Multicast?**

- **Aplicações em tempo real** (streaming, jogos online).
- **Distribuição de atualizações** (roteadores, servidores).
- **Economia de recursos** em comparação com broadcast ou múltiplos unicasts.


O multicast requer suporte da rede e configuração adequada nos roteadores. Nem todas as redes permitem seu uso indiscriminado.

##### Multicasting

O *multicasting* é mais amplo do que a comunicação *unicast* (ponto a ponto), mas mais restrito e direcionado do que a comunicação por *broadcast*.

O *multicasting* envia dados de um host para vários hosts diferentes, mas não para todos; os dados só são enviados para clientes que manifestaram interesse, juntando-se a um grupo de *multicast* específico.
De certa forma, isso é como uma reunião pública: as pessoas podem entrar e sair quando quiserem, deixando o grupo quando o assunto não lhes interessa mais.
Antes de entrarem e depois de saírem, elas não precisam processar a informação de forma alguma — os dados simplesmente não chegam até elas.

Na internet, essas "reuniões públicas" são melhor implementadas usando um *socket* de *multicast*, que envia uma cópia dos dados para um local (ou um grupo de locais) próximo das partes que declararam interesse neles. No melhor cenário, os dados são duplicados apenas quando chegam à rede local que atende aos clientes interessados — os dados atravessam a internet apenas uma vez.
De forma mais realista, várias cópias idênticas dos dados percorrem a internet, mas, ao escolher cuidadosamente os pontos onde os fluxos são duplicados, a carga na rede é minimizada.

A boa notícia é que programadores e administradores de rede não são responsáveis por escolher onde os dados são duplicados ou mesmo por enviar múltiplas cópias — os roteadores da internet cuidam de tudo isso.

O IP também suporta *broadcasting*, mas seu uso é estritamente limitado. Protocolos exigem *broadcasts* apenas quando não há alternativa, e os roteadores os restringem à rede ou sub-rede local, impedindo que se propaguem por toda a internet. Mesmo alguns poucos *broadcasts* globais pequenos poderiam sobrecarregar a internet.

Transmitir dados de alta largura de banda, como áudio, vídeo ou mesmo texto e imagens estáticas, está completamente fora de questão. Um único *spam* enviado para milhões de endereços já é ruim o suficiente — imagine o que aconteceria se um vídeo em tempo real fosse copiado para todos os bilhões de usuários da internet, quer eles quisessem assistir ou não.

No entanto, há um meio-termo entre a comunicação ponto a ponto e o *broadcast* para o mundo inteiro. Não faz sentido enviar um fluxo de vídeo para hosts que não estão interessados; precisamos de uma tecnologia que envie dados apenas para quem os deseja, sem incomodar o resto do mundo. Uma forma de fazer isso é usar múltiplos fluxos *unicast*.

Se 1.000 clientes querem assistir a uma transmissão ao vivo da BBC, os dados são enviados mil vezes. Isso é ineficiente, pois duplica dados desnecessariamente, mas ainda é ordens de magnitude mais eficiente do que enviar por *broadcast* para todos os hosts da internet. Ainda assim, se o número de clientes interessados for grande o suficiente, eventualmente a largura de banda ou o poder de processamento se esgotará — provavelmente mais cedo do que tarde.

Outra abordagem para o problema é criar árvores de conexão estáticas, como fazem o Usenet News e alguns sistemas de conferência. Os dados são enviados do site de origem para outros servidores, que os replicam para outros servidores, que por fim os repassam aos clientes.

Cada cliente se conecta ao servidor mais próximo. Isso é mais eficiente do que enviar tudo para todos os clientes interessados via múltiplos *unicasts*, mas o esquema é confuso e começa a mostrar sua idade. Novos sites precisam ser conectados manualmente à árvore, que nem sempre reflete a melhor topologia possível em um dado momento, e os servidores ainda precisam manter várias conexões ponto a ponto com seus clientes, enviando os mesmos dados para cada um.

Seria melhor permitir que os roteadores da internet determinassem dinamicamente as melhores rotas para transmitir informações distribuídas e replicar os dados apenas quando absolutamente necessário. É aí que entra o *multicasting*.

Por exemplo, se você está transmitindo vídeo por *multicast* de Nova York e 20 pessoas em uma mesma LAN em Los Angeles estão assistindo, o fluxo será enviado para aquela LAN apenas uma vez. Se mais 50 pessoas estiverem assistindo em São Francisco, o fluxo de dados será duplicado em algum lugar (digamos, Fresno) e enviado para as duas cidades.

Se mais 100 pessoas estiverem assistindo em Houston, outro fluxo será enviado para lá (talvez a partir de St. Louis). Os dados cruzaram a internet apenas três vezes, em vez das 170 vezes que seriam necessárias com conexões ponto a ponto ou dos milhões de vezes que um *broadcast* verdadeiro exigiria.

O *multicasting* está no meio do caminho entre a comunicação ponto a ponto, comum na internet, e o modelo de *broadcast* da televisão — e é mais eficiente do que ambos.

Quando um pacote é transmitido por *multicast*, ele é endereçado a um grupo de *multicast* e enviado a cada host pertencente a esse grupo. Ele não vai para um único host (como no *unicast*) nem para todos os hosts (como no *broadcast*), pois qualquer um desses extremos seria ineficiente.

![[../../Pasted image 20250814163811.png]]

Quando as pessoas falam sobre *multicasting*, áudio e vídeo são as primeiras aplicações que vêm à mente. De fato, a BBC vem conduzindo um teste de *multicast* que cobre tanto TV quanto rádio há vários anos, embora a participação dos ISPs tenha sido lamentavelmente limitada. No entanto, áudio e vídeo são apenas a ponta do iceberg.

Outras possibilidades incluem jogos multiplayer, sistemas de arquivos distribuídos, computação massivamente paralela, conferências com múltiplos participantes, replicação de bancos de dados, redes de entrega de conteúdo (*content delivery networks*) e muito mais.

O *multicasting* pode ser usado para implementar serviços de nomes e diretórios que não exigem que o cliente saiba o endereço de um servidor antecipadamente. Para procurar um nome, um host poderia enviar sua requisição por *multicast* para um endereço bem conhecido e aguardar até receber uma resposta do servidor mais próximo.

O Bonjour da Apple (também conhecido como Zeroconf) e o River da Apache ambos usam *multicasting* IP para descobrir dinamicamente serviços na rede local.

O *multicasting* foi projetado para se integrar à internet da forma mais transparente possível. A maior parte do trabalho é feita pelos roteadores e deve ser invisível para os programadores de aplicações.

Um aplicativo simplesmente envia pacotes de datagrama para um endereço de *multicast*, que não é fundamentalmente diferente de qualquer outro endereço IP. Os roteadores se encarregam de entregar o pacote a todos os hosts do grupo de *multicast*.

O maior problema é que os roteadores com suporte a *multicast* ainda não são onipresentes. Portanto, é preciso saber o suficiente sobre eles para descobrir se o *multicasting* é suportado em sua rede.

Por exemplo, embora a BBC já utilize *multicast* há vários anos, seus fluxos de *multicast* só estão acessíveis para assinantes de cerca de uma dúzia de ISPs britânicos relativamente pequenos. Na prática, o *multicasting* é muito mais usado atrás de firewalls, dentro de uma única organização, do que através da internet global.

Quanto ao aplicativo em si, é preciso prestar atenção a um campo adicional no cabeçalho dos datagramas, chamado *Time-To-Live* (TTL, Tempo de Vida). O TTL é o número máximo de roteadores que um datagrama pode atravessar. Depois de passar por essa quantidade de roteadores, o pacote é descartado. O *multicasting* usa o TTL como uma forma *ad hoc* de limitar a distância que um pacote pode percorrer.

Por exemplo, você não quer que pacotes de um jogo local de *Dogfight* cheguem a roteadores do outro lado do mundo. A Figura mostra como os valores de TTL limitam a propagação de um pacote.


> Eu, muito interresante.
> Se for para ficar na lan use 0, se for apra passar para o roteador ou rede seguinte use 1. Ja que a cada roteador e decrementado -1.


##### Multicast Addresses and Groups


Um *endereço de multicast* é o endereço compartilhado por um grupo de hosts, chamado de *grupo de multicast*. Primeiro, vamos falar sobre o endereço.

No IPv4, os endereços de *multicast* são endereços IP dentro do bloco CIDR **224.0.0.0/4** (ou seja, variam de **224.0.0.0** a **239.255.255.255**). Todos os endereços nessa faixa têm os quatro primeiros bits **1110** em binário. Já no IPv6, os endereços de *multicast* estão no bloco **ff00::/8** (ou seja, começam com o byte **0xFF**, que equivale a **11111111** em binário).

Assim como qualquer endereço IP, um endereço de *multicast* pode ter um nome de host. Por exemplo, o endereço **224.0.1.1** (usado pelo *Network Time Protocol* como um serviço distribuído) tem o nome **ntp.mcast.net**.

Um **grupo de multicast** é um conjunto de hosts na internet que compartilham um mesmo endereço de *multicast*. Qualquer dado enviado para esse endereço é retransmitido a todos os membros do grupo. A participação em um grupo de *multicast* é aberta: hosts podem entrar ou sair do grupo a qualquer momento.

Os grupos podem ser **permanentes** ou **transitórios**:
- **Grupos permanentes** têm endereços fixos, que não mudam, independentemente de terem membros ativos ou não.
- Já a maioria dos grupos de *multicast* são **transitórios** e existem apenas enquanto possuem membros.

Para criar um novo grupo de *multicast*, basta escolher um endereço aleatório entre **225.0.0.0** e **238.255.255.255**, criar um objeto `InetAddress` para esse endereço e começar a enviar dados.

A **IANA** (Internet Assigned Numbers Authority) é responsável por atribuir endereços de multicast permanentes conforme a necessidade. Até agora, algumas centenas foram especificamente reservados.

Os **endereços de multicast link-local** começam com **224.0.0** (ou seja, endereços de **224.0.0.0** a **224.0.0.255**) e são reservados para protocolos de roteamento e outras atividades de baixo nível, como descoberta de gateways e relatórios de membros de grupo.

Por exemplo, **all-systems.mcast.net (224.0.0.1)** é um grupo de multicast que inclui todos os sistemas na sub-rede local. Roteadores de multicast **nunca** encaminham datagramas com destinos nessa faixa. A Tabela lista alguns desses endereços reservados.

>Eu, isso ta bem desatualizado ainda n tive tempo de criar uma tabela disso

**Tabela de Endereços de Multicast**

| Nome de domínio | Endereço IP | Finalidade |
|----------------|------------|------------|
| BASE-ADDRESS.MCAST.NET | 224.0.0.0 | Endereço base reservado. Nunca é atribuído a nenhum grupo multicast. |
| ALL-SYSTEMS.MCAST.NET | 224.0.0.1 | Todos os sistemas na sub-rede local. |
| ALL-ROUTERS.MCAST.NET | 224.0.0.2 | Todos os roteadores na sub-rede local. |
| DVMRP.MCAST.NET | 224.0.0.4 | Todos os roteadores que usam o Distance Vector Multicast Routing Protocol (DVMRP) nesta sub-rede. |
| MOBILE-AGENTS.MCAST.NET | 224.0.0.11 | Agentes móveis na sub-rede local. |
| DHCP-AGENTS.MCAST.NET | 224.0.0.12 | Permite que um cliente localize um servidor DHCP ou agente de retransmissão na sub-rede local. |
| RSVPENCAPSULATION.MCAST.NET | 224.0.0.14 | Encapsulamento RSVP (Resource reSerVation Protocol) nesta sub-rede, um protocolo para reserva de banda garantida na internet. |
| VRRP.MCAST.NET | 224.0.0.18 | Roteadores que usam o Virtual Router Redundancy Protocol (VRRP). |
| - | 224.0.0.35 | DXCluster, usado para anunciar estações de rádio amador (DX) estrangeiras. |
| - | 224.0.0.36 | Digital Transmission Content Protection (DTCP), tecnologia de DRM que criptografa conexões entre DVD players, TVs e dispositivos similares. |
| - | 224.0.0.37-224.0.0.68 | Endereçamento zeroconf. |
| - | 224.0.0.106 | Descoberta de Roteadores Multicast. |
| - | 224.0.0.112 | Descoberta de Dispositivos de Gerenciamento Multipath. |
| - | 224.0.0.113 | Qualcomm's AllJoyn. |
| - | 224.0.0.114 | Protocolo Inter RFID Reader. |
| - | 224.0.0.251 | Multicast DNS (mDNS), usado para auto-atribuição e resolução de nomes de hosts em endereços multicast. |
| - | 224.0.0.252 | Link-local Multicast Name Resolution (precursor do mDNS), permite que nós atribuam nomes de domínio apenas para a rede local. |
| - | 224.0.0.253 | Usado pelo Teredo para tunelamento de IPv6 sobre IPv4. Outros clientes Teredo na mesma sub-rede IPv4 respondem a este endereço. |
| - | 224.0.0.254 | Reservado para experimentação. |

Os endereços multicast permanentemente atribuídos que se estendem além da sub-rede local começam com **224.1.** ou **224.2.** A Tabela 13-2 lista alguns desses endereços permanentes. Alguns blocos de endereços, variando em tamanho de algumas dezenas a alguns milhares, também foram reservados para propósitos específicos.

A lista completa está disponível no site **iana.org**, embora você deva observar que ela contém muitos serviços, protocolos e empresas que já não existem. Os cerca de 248 milhões de endereços multicast restantes podem ser usados temporariamente por qualquer pessoa que precise deles.

Os roteadores multicast (abreviados como **mrouters**) são responsáveis por garantir que dois sistemas diferentes não tentem usar o mesmo endereço simultaneamente.



**Endereços multicast permanentes comuns**

| Nome de domínio | Endereço IP | Finalidade |
|----------------|------------|------------|
| NTP.MCAST.NET | 224.0.1.1 | Protocolo de Tempo de Rede (Network Time Protocol) |
| NSS.MCAST.NET | 224.0.1.6 | Servidor de Serviço de Nomes (Name Service Server) |
| AUDIONEWS.MCAST.NET | 224.0.1.7 | Transmissão multicast de notícias em áudio |
| MTP.MCAST.NET | 224.0.1.9 | Protocolo de Transporte Multicast (Multicast Transport Protocol) |
| IETF-1-LOW-AUDIO.MCAST.NET | 224.0.1.10 | Canal 1 de áudio de baixa qualidade de reuniões do IETF |
| IETF-1-AUDIO.MCAST.NET | 224.0.1.11 | Canal 1 de áudio de alta qualidade de reuniões do IETF |
| IETF-1-VIDEO.MCAST.NET | 224.0.1.12 | Canal 1 de vídeo de reuniões do IETF |
| IETF-2-LOW-AUDIO.MCAST.NET | 224.0.1.13 | Canal 2 de áudio de baixa qualidade de reuniões do IETF |
| IETF-2-AUDIO.MCAST.NET | 224.0.1.14 | Canal 2 de áudio de alta qualidade de reuniões do IETF |
| IETF-2-VIDEO.MCAST.NET | 224.0.1.15 | Canal 2 de vídeo de reuniões do IETF |
| MLOADD.MCAST.NET | 224.0.1.19 | MLOADD mede a carga de tráfego em interfaces de rede usando multicast para comunicação |
| EXPERIMENT.MCAST.NET | 224.0.1.20 | Experimentos |
| - | 224.0.23.178 | Protocolo de Descoberta Java (JDP) para localizar JVMs gerenciáveis na rede |
| MICROSOFT.MCAST.NET | 224.0.1.24 | Usado pelo Windows Internet Name Service (WINS) para localização de servidores |
| MTRACE.MCAST.NET | 224.0.1.32 | Versão multicast do traceroute |
| JINI-ANNOUNCEMENT.MCAST.NET | 224.0.1.84 | Anúncios JINI |
| JINI-REQUEST.MCAST.NET | 224.0.1.85 | Requisições JINI |
| - | 224.0.1.143 | Rede de Informação Meteorológica para Gestores de Emergência |
| - | 224.2.0.0-224.2.255.255 | Endereços da Multicast Backbone na Internet (MBONE) para conferências multimídia |
| - | 224.2.2.2 | Porta 9875 para transmissão da programação MBONE disponível |
| - | 239.0.0.0-239.255.255.255 | Escopo organizacional local, usando diferentes faixas para restringir tráfego multicast |



##### Clients and Servers

Quando um host deseja enviar dados para um grupo multicast, ele encapsula esses dados em datagramas multicast, que nada mais são do que datagramas UDP endereçados a um grupo multicast. Os dados multicast são enviados via UDP, que, embora não seja confiável, pode ser até três vezes mais rápido do que dados enviados via TCP orientado a conexão. (Se você pensar bem, multicast sobre TCP seria praticamente inviável. O TCP exige que os hosts confirmem o recebimento dos pacotes, e lidar com essas confirmações em um cenário multicast seria um pesadelo.)

Se você estiver desenvolvendo uma aplicação multicast que não pode tolerar perda de dados, é sua responsabilidade verificar se os dados foram danificados durante o trânsito e decidir como lidar com dados faltantes. Por exemplo, se você estiver construindo um sistema de cache distribuído, pode simplesmente optar por excluir da cache quaisquer arquivos que não cheguem intactos.

Como mencionado anteriormente, do ponto de vista do programador de aplicações, a principal diferença entre multicast e sockets UDP convencionais é a necessidade de se preocupar com o valor TTL (Time-To-Live). Este é um único byte no cabeçalho IP que pode assumir valores de 1 a 255 e é interpretado aproximadamente como o número de roteadores pelos quais um pacote pode passar antes de ser descartado.

Cada vez que o pacote passa por um roteador, seu campo TTL é decrementado em pelo menos um (alguns roteadores podem decrementar o TTL em dois ou mais). Quando o TTL chega a zero, o pacote é descartado. O campo TTL foi originalmente projetado para evitar loops de roteamento, garantindo que todos os pacotes eventualmente sejam descartados, impedindo que roteadores mal configurados fiquem enviando pacotes indefinidamente.

No multicast IP, o TTL limita geograficamente o alcance do multicast. Por exemplo:
- Um TTL de 16 limita o pacote à área local (geralmente uma organização ou talvez uma organização e seus vizinhos imediatos)
- Um TTL de 127 envia o pacote para todo o mundo
- Valores intermediários também são possíveis

No entanto, não existe uma maneira precisa de mapear valores TTL para distâncias geográficas. Geralmente, quanto mais distante um site estiver, mais roteadores o pacote precisará atravessar. Pacotes com valores TTL pequenos não viajarão tão longe quanto pacotes com valores TTL grandes.

**Observação importante:** Pacotes endereçados a um grupo multicast entre 224.0.0.0 e 224.0.0.255 nunca são encaminhados além da sub-rede local, independentemente dos valores TTL utilizados.

> Eu, achava que era so determinar o ttl apra fazser o padote sair da rede local, mas aparentemente não é.

| Alcance do destino                                                                                     | Valor TTL |
| ------------------------------------------------------------------------------------------------------ | --------- |
| O host local                                                                                           | 0         |
| A sub-rede local                                                                                       | 1         |
| O campus local (mesmo lado do roteador de Internet mais próximo), mas possivelmente em LANs diferentes | 16        |
| Sites de alta largura de banda no mesmo país, geralmente próximos ao backbone                          | 32        |
| Todos os sites no mesmo país                                                                           | 48        |
| Todos os sites no mesmo continente                                                                     | 64        |
| Sites de alta largura de banda em todo o mundo                                                         | 128       |
| Todos os sites em todo o mundo                                                                         | 255       |

Depois que os dados são encapsulados em um ou mais datagramas, o host transmissor os envia para a Internet. Esse processo é idêntico ao envio de dados UDP convencionais (unicast).

O host transmissor começa enviando um datagrama multicast para a rede local. Esse pacote atinge imediatamente todos os membros do grupo multicast na mesma sub-rede.

Se o campo Time-To-Live (TTL) do pacote for maior que 1, os roteadores multicast na rede local encaminham o pacote para outras redes que possuam membros do grupo de destino.

Quando o pacote chega a um dos destinos finais, o roteador multicast na rede remeta transmite o pacote para cada host sob sua responsabilidade que pertença ao grupo multicast.

Se necessário, o roteador multicast também retransmite o pacote para os próximos roteadores no caminho entre o roteador atual e todos os seus destinos finais.

Quando os dados chegam a um host membro do grupo multicast, ele os recebe como qualquer outro datagrama UDP - mesmo que o endereço de destino do pacote não corresponda exatamente ao host receptor.

O host reconhece que o datagrama é destinado a ele porque pertence ao grupo multicast para o qual o datagrama foi endereçado, assim como a maioria de nós aceita correspondência endereçada a "Morador", mesmo que ninguém se chame Sr. ou Sra. Morador.

O host receptor deve estar escutando na porta adequada, pronto para processar o datagrama quando ele chegar.

##### Routers and Routing

Uma das configurações multicast mais simples possíveis: um único servidor enviando os mesmos dados para quatro clientes atendidos pelo mesmo roteador. Um socket multicast envia um único fluxo de dados pela Internet até o roteador dos clientes; o roteador então duplica esse fluxo e o envia para cada um dos clientes.

Sem sockets multicast, o servidor precisaria enviar quatro fluxos separados (porém idênticos) de dados para o roteador, que por sua vez encaminharia cada fluxo para um cliente. Ao usar um único fluxo para enviar os mesmos dados a múltiplos clientes, reduz-se significativamente a largura de banda necessária no backbone da Internet.

É claro que rotas do mundo real podem ser muito mais complexas, envolvendo múltiplas hierarquias de roteadores redundantes. Porém, o objetivo dos sockets multicast é simples: não importa quão complexa seja a rede, os mesmos dados nunca devem ser enviados mais de uma vez em qualquer segmento de rede específico.

Felizmente, você não precisa se preocupar com questões de roteamento. Basta criar um MulticastSocket, fazer com que o socket entre em um grupo multicast e inserir o endereço do grupo multicast no DatagramPacket que deseja enviar. Os roteadores e a classe MulticastSocket cuidam do resto.

**A principal limitação do multicast** é a disponibilidade de roteadores multicast especiais (mrouters). Os mrouters são roteadores de Internet reconfigurados ou workstations que suportam as extensões de multicast IP. Muitos ISPs voltados para consumidores deliberadamente não habilitam o multicast em seus roteadores. Em 2023 (atualizado), ainda é possível encontrar hosts entre os quais não existe uma rota multicast (ou seja, não há um caminho entre os hosts que percorra exclusivamente mrouters).

Para enviar e receber dados multicast além da sub-rede local, você precisa de um roteador multicast. Consulte seu administrador de rede para verificar se seus roteadores suportam multicast. Você também pode tentar executar um ping para `all-routers.mcast.net`. Se algum roteador responder, sua rede está conectada a um roteador multicast:

>Eu, isso não funciona
>Mas usar ipv6 pingando em um ip multicast ipv6 funciona de fato

```
% ping all-routers.mcast.net
all-routers.mcast.net is alive
```

Mesmo assim, isso pode não permitir que você envie ou receba de todos os hosts com capacidade multicast na Internet. Para que seus pacotes alcancem um host específico, deve haver um caminho de roteadores com capacidade multicast entre seu host e o host remoto. Alternativamente, alguns sites podem estar conectados por software especial de túnel multicast que transmite dados multicast sobre UDP unicast, que todos os roteadores entendem.

Se você tiver problemas para obter os resultados esperados com os exemplos deste capítulo, verifique com seu administrador de rede local ou ISP se o multicast é realmente suportado por seus roteadores.


##### Working with Multicast Sockets

Em Java, você implementa multicast utilizando a classe `java.net.MulticastSocket`, uma subclasse de `java.net.DatagramSocket`:

```java
public class MulticastSocket extends DatagramSocket
    implements Closeable, AutoCloseable
```

Como seria de se esperar, o comportamento do `MulticastSocket` é muito similar ao do `DatagramSocket`: você coloca seus dados em objetos `DatagramPacket` que são enviados e recebidos através do `MulticastSocket`. Portanto, não repetirei os conceitos básicos - esta discussão assume que você já conhece o trabalho com datagramas. Se você está pulando entre capítulos ao invés de ler sequencialmente, talvez seja um bom momento para voltar e ler o Capítulo 12.

**Para receber dados multicast:**

1. Primeiro, crie um `MulticastSocket` com o construtor padrão, especificando a porta para escuta. Este fragmento abre um socket na porta 2300:

```java
MulticastSocket ms = new MulticastSocket(2300);
```

2. Em seguida, entre em um grupo multicast usando o método `joinGroup()`:

```java
InetAddress group = InetAddress.getByName("224.2.2.2");
ms.joinGroup(group);
```

Esta operação:
- Sinaliza aos roteadores no caminho entre você e o servidor para começar a enviar dados
- Informa ao host local que ele deve repassar os pacotes IP endereçados ao grupo multicast

Depois de entrar em um grupo multicast, você recebe dados UDP da mesma forma que faria com um `DatagramSocket`. Você cria um `DatagramPacket` com um array de bytes que serve como buffer para os dados e entra em um loop onde recebe os dados chamando o método `receive()`, herdado da classe `DatagramSocket`:

```java
byte[] buffer = new byte[8192];
DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
ms.receive(dp);
```

Quando você não quiser mais receber dados, saia do grupo multicast invocando o método `leaveGroup()` do socket. Em seguida, pode fechar o socket com o método `close()` herdado de `DatagramSocket`:

```java
ms.leaveGroup(group);
ms.close();
```

**Para enviar dados** para um endereço multicast, o processo é similar ao envio de dados UDP para um endereço unicast. Você não precisa entrar em um grupo multicast para enviar dados a ele. Basta criar um novo `DatagramPacket`, inserir os dados e o endereço do grupo multicast no pacote, e enviá-lo com o método `send()`:

```java
InetAddress ia = InetAddress.getByName("experiment.mcast.net");
byte[] data = "Aqui vão alguns dados multicast\r\n".getBytes("UTF-8");
int port = 4000;
DatagramPacket dp = new DatagramPacket(data, data.length, ia, port);
MulticastSocket ms = new MulticastSocket();
ms.send(dp);
```

**Uma importante ressalva:** sockets multicast representam uma vulnerabilidade de segurança considerável. Por isso, código não confiável executado sob um `SecurityManager` não tem permissão para realizar operações com sockets multicast. Normalmente, código carregado remotamente só pode enviar ou receber datagramas do host de origem.

No entanto, sockets multicast não permitem esse tipo de restrição - uma vez que você envia dados por um socket multicast, tem controle limitado e não confiável sobre quais hosts receberão esses dados. Por isso, a maioria dos ambientes que executam código remoto adota a abordagem conservadora de desabilitar completamente o multicast.

##### The Constructors

Os construtores são simples. Você pode escolher uma porta específica para escutar ou deixar o Java atribuir uma porta anônima:

```java
public MulticastSocket() throws SocketException
public MulticastSocket(int port) throws SocketException
public MulticastSocket(SocketAddress bindAddress) throws IOException
```

Exemplos de uso:
```java
MulticastSocket ms1 = new MulticastSocket();  // Porta anônima
MulticastSocket ms2 = new MulticastSocket(4000);  // Porta específica

SocketAddress address = new InetSocketAddress("192.168.254.32", 4000);
MulticastSocket ms3 = new MulticastSocket(address);  // Endereço específico
```

**Tratamento de erros:**

Todos os três construtores lançam uma `SocketException` se o socket não puder ser criado. Isso ocorre se:
- Você não tiver privilégios suficientes para vincular à porta
- A porta que você está tentando usar já estiver ocupada

**Observações importantes:**
1. Um `MulticastSocket` não pode ocupar uma porta já usada por um `DatagramSocket` (e vice-versa), pois para o sistema operacional ambos são sockets de datagrama.

2. Você pode passar `null` para o construtor para criar um socket não vinculado, que será conectado posteriormente com o método `bind()`. Isso é útil para configurar opções de socket que só podem ser definidas antes da vinculação.

Exemplo de uso avançado (desabilitando SO_REUSEADDR):
```java
MulticastSocket ms = new MulticastSocket(null);
ms.setReuseAddress(false);  // Desabilita a reutilização de endereço
SocketAddress address = new InetSocketAddress(4000);
ms.bind(address);  // Vincula após configurar as opções
```

**Notas técnicas:**
- "Porta anônima" refere-se a uma porta atribuída automaticamente pelo sistema
- `SO_REUSEADDR` é uma opção de socket normalmente habilitada por padrão para sockets multicast
- A vinculação tardia (`bind()`) permite configurações mais flexíveis de opções de socket

##### Communicating with a Multicast Group

Uma vez criado um `MulticastSocket`, ele pode realizar quatro operações principais:

1. Entrar em um grupo multicast (join)
2. Enviar dados para os membros do grupo
3. Receber dados do grupo
4. Sair do grupo multicast (leave)

A classe `MulticastSocket` fornece métodos específicos para as operações 1 e 4. Para enviar e receber dados (operações 2 e 3), utilizamos os métodos herdados da superclasse `DatagramSocket`:

```java
// Métodos específicos para multicast
joinGroup(InetAddress multicastAddr)  // Entra no grupo
leaveGroup(InetAddress multicastAddr) // Sai do grupo

// Métodos herdados de DatagramSocket
send(DatagramPacket p)               // Envia dados
receive(DatagramPacket p)            // Recebe dados
```

**Regras de utilização:**
- Você deve entrar em um grupo **antes** de poder receber dados dele
- Não é necessário entrar em um grupo para enviar dados para ele
- É possível alternar livremente entre envio e recebimento de dados
- A ordem das operações é flexível, exceto pela necessidade de entrar no grupo antes do recebimento

**Exemplo de fluxo típico:**
1. Criar o socket multicast
2. Entrar no grupo desejado
3. Enviar/receber dados conforme necessário
4. Quando finalizar, sair do grupo
5. Fechar o socket

Esta abordagem permite comunicação bidirecional flexível em grupos multicast, mantendo a simplicidade da API de datagramas enquanto adiciona o suporte necessário para funcionalidades multicast específicas.


##### Joining groups


Para ingressar em um grupo multicast, passe um `InetAddress` ou um `SocketAddress` do grupo multicast para o método `joinGroup()`:

```java
public void joinGroup(InetAddress address) throws IOException
public void joinGroup(SocketAddress address, NetworkInterface interface) throws IOException
```

Após ingressar em um grupo multicast, você recebe datagramas exatamente como faria com datagramas unicast, conforme mostrado no capítulo anterior. Ou seja, você configura um `DatagramPacket` como buffer e o passa para o método `receive()` do socket. Por exemplo:

```java
try {
    MulticastSocket ms = new MulticastSocket(4000);
    InetAddress ia = InetAddress.getByName("224.2.2.2");
    ms.joinGroup(ia);
    byte[] buffer = new byte[8192];
    while (true) {
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        ms.receive(dp);
        String s = new String(dp.getData(), "8859_1");
        System.out.println(s);
    }
} catch (IOException ex) {
    System.err.println(ex);
}
```

Se o endereço que você tentar ingressar não for um endereço multicast (ou seja, não estiver entre 224.0.0.0 e 239.255.255.255), o método `joinGroup()` lançará um `IOException`.

###### Funcionalidades Avançadas:

1. **Múltiplos Grupos**: Um único `MulticastSocket` pode ingressar em vários grupos multicast. As informações sobre associação a grupos são armazenadas em roteadores multicast, não no objeto.

2. **Múltiplos Sockets**: Vários sockets multicast na mesma máquina (ou mesmo no mesmo programa Java) podem ingressar no mesmo grupo. Nesse caso, cada socket recebe uma cópia completa dos dados destinados àquele grupo.

3. **Interface de Rede Específica**: Um segundo argumento permite ingressar em um grupo multicast apenas em uma interface de rede local específica. Por exemplo:

```java
MulticastSocket ms = new MulticastSocket();
SocketAddress group = new InetSocketAddress("224.2.2.2", 40);
NetworkInterface ni = NetworkInterface.getByName("eth0");
if (ni != null) {
    ms.joinGroup(group, ni);
} else {
    ms.joinGroup(group);
}
```

Exceto pelo argumento adicional que especifica a interface de rede, esse método se comporta de maneira semelhante ao `joinGroup()` de um único argumento. Passar um `SocketAddress` que não represente um grupo multicast como primeiro argumento lançará um `IOException`.


##### Leaving groups and closing the connection


Utilize o método `leaveGroup()` quando não desejar mais receber datagramas do grupo multicast especificado, seja em todas as interfaces de rede ou em uma específica:

```java
public void leaveGroup(InetAddress address) throws IOException
public void leaveGroup(SocketAddress multicastAddress, NetworkInterface interface) throws IOException
```

Este método sinaliza ao roteador multicast local para parar de enviar datagramas para você. Se o endereço fornecido não for um endereço multicast válido (ou seja, não estiver entre 224.0.0.0 e 239.255.255.255), o método lançará um `IOException`. No entanto, não ocorrerá exceção se você tentar sair de um grupo ao qual nunca ingressou.

###### Tratamento de Exceções:

Quase todos os métodos do `MulticastSocket` podem lançar `IOException`, portanto é recomendado envolver as operações em blocos `try-catch`.

**Em Java 7+** (com try-with-resources):

```java
try (MulticastSocket socket = new MulticastSocket()) {
    // Conectar ao servidor...
} catch (IOException ex) {
    ex.printStackTrace();
}
```

**Em Java 6 e versões anteriores** (com bloco finally):

```java
MulticastSocket socket = null;
try {
    socket = new MulticastSocket();
    // Conectar ao servidor...
} catch (IOException ex) {
    ex.printStackTrace();
} finally {
    if (socket != null) {
        try {
            socket.close();
        } catch (IOException ex) {
            // Exceção ignorada
        }
    }
}
```

###### Observações Importantes:
1. O fechamento adequado do socket libera recursos do sistema
2. O bloco `finally` garante que o socket será fechado mesmo se ocorrerem exceções
3. Em Java 7+, o try-with-resources simplifica o gerenciamento de recursos
4. É seguro chamar `leaveGroup()` mesmo sem ter chamado `joinGroup()` anteriormente

##### Sending multicast data

**Envio de dados com MulticastSocket**

O envio de dados com um `MulticastSocket` é semelhante ao uso de um `DatagramSocket`. Você encapsula os dados em um objeto `DatagramPacket` e os envia usando o método `send()` herdado de `DatagramSocket`. Os dados são enviados para todos os hosts pertencentes ao grupo multicast especificado no pacote. Exemplo:

```java
try {
    InetAddress ia = InetAddress.getByName("experiment.mcast.net");
    byte[] data = "Aqui estão alguns dados multicast\r\n".getBytes();
    int port = 4000;
    DatagramPacket dp = new DatagramPacket(data, data.length, ia, port);
    MulticastSocket ms = new MulticastSocket();
    ms.send(dp);
} catch (IOException ex) {
    System.err.println(ex);
}
```

**Configuração do TTL (Time-To-Live):**

Por padrão, sockets multicast usam TTL=1 (os pacotes não saem da sub-rede local). Você pode alterar este valor para cada pacote (de 0 a 255):

```java
public void setTimeToLive(int ttl) throws IOException
public int getTimeToLive() throws IOException
```

Exemplo com TTL=64:
```java
try {
    InetAddress ia = InetAddress.getByName("experiment.mcast.net");
    byte[] data = "Aqui estão alguns dados multicast\r\n".getBytes();
    int port = 4000;
    DatagramPacket dp = new DatagramPacket(data, data.length, ia, port);
    MulticastSocket ms = new MulticastSocket();
    ms.setTimeToLive(64);  // Configura TTL para 64 saltos
    ms.send(dp);
} catch (IOException ex) {
    System.err.println(ex);
}
```

**Pontos importantes:**
1. O método `setTimeToLive()` define o TTL padrão para todos os pacotes enviados
2. O TTL controla quantos roteadores o pacote pode atravessar (0-255)
3. Valores mais altos permitem que os pacotes alcancem redes mais distantes
4. O envio multicast não requer ingresso no grupo de destino
5. Todos os hosts do grupo especificado receberão os pacotes enviados

**Nota técnica:** A codificação de caracteres no exemplo foi ajustada para o padrão português, mantendo os termos técnicos originais como "TTL", "MulticastSocket" e "DatagramPacket".

##### Loopback mode

O comportamento de um host ao receber seus próprios pacotes multicast varia conforme a plataforma - isto é, se há ou não retorno (loopback) dos pacotes enviados. Os métodos para configurar este comportamento são:

```java
public void setLoopbackMode(boolean disable) throws SocketException
public boolean getLoopbackMode() throws SocketException
```

**Funcionamento:**
- `setLoopbackMode(true)`: Desativa o loopback (não recebe os próprios pacotes)
- `setLoopbackMode(false)`: Ativa o loopback (recebe os próprios pacotes)

**Observações importantes:**
1. Este é apenas um indicativo - as implementações podem ignorar sua configuração
2. O método `getLoopbackMode()` retorna:
    - `true` se os pacotes NÃO estão sendo retornados (loopback desativado)
    - `false` se os pacotes ESTÃO sendo retornados (loopback ativado)

**Nota:** Esta lógica inversa pode ser confusa (provavelmente seguindo a convenção questionável de que padrões devem ser `true`).

**Tratamento confiável:**
1. Se o sistema faz loopback e você não quer:
    - Implemente reconhecimento e descarte dos próprios pacotes
2. Se o sistema não faz loopback e você quer:
    - Armazene cópias dos pacotes enviados
    - Insira-os manualmente nas estruturas internas de dados

**Exemplo de uso:**
```java
MulticastSocket ms = new MulticastSocket();
try {
    ms.setLoopbackMode(true); // Tenta desativar loopback
    boolean loopbackState = ms.getLoopbackMode();
    System.out.println("Loopback está " + (loopbackState ? "desativado" : "ativado"));
} catch (SocketException ex) {
    System.err.println("Erro ao configurar loopback: " + ex);
}
```

**Conclusão:** Embora você possa solicitar um comportamento com `setLoopback()`, não pode contar com ele - é essencial verificar o estado real com `getLoopbackMode()` e implementar soluções alternativas conforme necessário.


##### Network interfaces

Em hosts com múltiplas interfaces de rede, os métodos `setInterface()` e `setNetworkInterface()` permitem selecionar qual interface será usada para envio e recebimento multicast:

```java
public void setInterface(InetAddress address) throws SocketException
public InetAddress getInterface() throws SocketException
public void setNetworkInterface(NetworkInterface interface) throws SocketException
public NetworkInterface getNetworkInterface() throws SocketException
```

**Principais características:**
1. Os métodos setters lançam `SocketException` se o argumento não corresponder a uma interface local
2. A interface pode ser configurada tanto por endereço IP (`InetAddress`) quanto por nome de interface (`NetworkInterface`)
3. Ao contrário de sockets unicast, a interface em MulticastSocket é mutável

**Exemplo de uso com `setInterface()`:**
```java
try {
    InetAddress ia = InetAddress.getByName("www.ibiblio.org");
    MulticastSocket ms = new MulticastSocket(2048);
    ms.setInterface(ia);
    // operações de envio/recebimento...
} catch (UnknownHostException ue) {
    System.err.println(ue);
} catch (SocketException se) {
    System.err.println(se);
}
```

**Diferença entre os métodos:**
- `setInterface()`: configura por endereço IP (ex: "192.168.1.100")
- `setNetworkInterface()`: configura por nome de interface (ex: "eth0")

**Métodos de obtenção:**
- `getInterface()` retorna um `InetAddress`
- `getNetworkInterface()` retorna um objeto `NetworkInterface`

**Comportamento padrão:**
Se nenhuma interface foi configurada explicitamente, `getNetworkInterface()` retorna um objeto placeholder com:
- Endereço: "0.0.0.0"
- Índice: -1

**Exemplo de verificação:**
```java
NetworkInterface intf = ms.getNetworkInterface();
System.out.println("Interface: " + intf.getName());
```

**Recomendação:** Configure a interface imediatamente após criar o socket e evite alterá-la posteriormente para garantir comportamento consistente.


##### Two Simple Examples

### Tradução para Português:

A maioria dos servidores multicast não faz distinção sobre com quem irá se comunicar. Portanto, é fácil ingressar em um grupo e monitorar os dados que estão sendo enviados. O Exemplo 13-1 apresenta a classe `MulticastSniffer` que:

1. Lê o nome de um grupo multicast da linha de comando
2. Constrói um `InetAddress` a partir desse nome
3. Cria um `MulticastSocket` que tenta ingressar no grupo

**Funcionamento:**
- Se bem-sucedido, o programa recebe datagramas e imprime seu conteúdo
- Útil principalmente para verificar recebimento de dados multicast
- A maioria dos dados multicast é binária e pode não ser legível como texto

```java
import java.io.*;
import java.net.*;

public class MulticastSniffer {
    public static void main(String[] args) {
        // Configuração inicial omitida para brevidade...
        
        try {
            MulticastSocket ms = new MulticastSocket(port);
            ms.joinGroup(group);
            
            byte[] buffer = new byte[8192];
            while (true) {
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                ms.receive(dp);
                String s = new String(dp.getData(), "8859_1");
                System.out.println(s);
            }
        } catch (IOException ex) {
            // Tratamento de erros
        } finally {
            // Limpeza de recursos
        }
    }
}
```

**Caso de Uso com UPnP:**
Dispositivos UPnP enviam mensagens HTTPU para:
- Endereço: 239.255.255.250
- Porta: 1900

Ao executar o programa nesse grupo, você verá mensagens de anúncio de dispositivos, como no exemplo de uma Google TV que envia anúncios frequentemente.

**Para enviar dados multicast**, o Exemplo 13-2 mostra a classe `MulticastSender`:

```java
public class MulticastSender {
    public static void main(String[] args) {
        // Configuração inicial...
        
        try (MulticastSocket ms = new MulticastSocket()) {
            ms.setTimeToLive(ttl);
            ms.joinGroup(ia);
            
            for (int i = 1; i < 10; i++) {
                ms.send(dp);
            }
            
            ms.leaveGroup(ia);
        } catch (IOException ex) {
            // Tratamento de erros
        }
    }
}
```

**Como testar:**
1. Execute `MulticastSniffer` em uma máquina:
   ```
   java MulticastSniffer all-systems.mcast.net 4000
   ```
2. Execute `MulticastSender` em outra máquina:
   ```
   java MulticastSender all-systems.mcast.net 4000
   ```

**Observações:**
- O TTL=1 limita o tráfego à sub-rede local
- Para funcionar além da sub-rede local, são necessários roteadores multicast
- O receptor deve ser iniciado antes do transmissor

**Saída esperada no receptor:**
```
Here's some multicast data
Here's some multicast data
... (10 vezes)
```

Esta implementação demonstra os conceitos básicos de comunicação multicast em Java, mostrando tanto o envio quanto o recebimento de dados em um grupo multicast.


> Eu, obrigado pela sua determinação GIL


