
## Capitulo 8 - Sockets for Clients


##### **1. Transmissão de Dados na Internet**  
- Os dados são enviados em **pacotes (datagramas)**, cada um contendo:  
  - **Cabeçalho (header):**  
    - Endereço de origem/destino (IP + porta).  
    - Checksum (para detecção de erros).  
    - Metadados para controle de fluxo e confiabilidade.  
  - **Payload (dados úteis).**  
- **Desafios:**  
  - Dados grandes são divididos em múltiplos pacotes.  
  - Pacotes podem ser **perdidos, corrompidos ou chegar fora de ordem**, exigindo retransmissão e reorganização.  

##### **2. O Papel dos Sockets**  
- **Abstração simplificada:** Sockets escondem a complexidade do gerenciamento de pacotes, permitindo tratar a conexão como um **fluxo de dados** (stream).  
- **Operações básicas de um socket (cliente/servidor):**  
  1. Conectar-se a um host remoto.  
  2. Enviar dados.  
  3. Receber dados.  
  4. Fechar a conexão.  
  5. (Servidor) Vincular-se a uma porta.  
  6. (Servidor) Escutar por conexões.  
  7. (Servidor) Aceitar conexões.  

##### **3. Uso de Sockets em Java**  
- **Classe `Socket` (cliente):**  
  - **Exemplo de fluxo típico:**  
    ```java  
    Socket socket = new Socket("host", porta); // Conecta ao servidor  
    OutputStream out = socket.getOutputStream(); // Para enviar dados  
    InputStream in = socket.getInputStream();    // Para receber dados  
    // ... Lógica de comunicação ...  
    socket.close(); // Fecha a conexão  
    ```  
  - **Comunicação full-duplex:** Ambos os lados podem enviar/receber dados simultaneamente.  
- **Protocolos:** O significado dos dados depende do protocolo (ex: HTTP, FTP, SMTP).  

##### **4. Protocolos e Experimentação com Telnet**  
- **Telnet como ferramenta de teste:** Permite simular clientes manualmente para entender protocolos.  
  - **Exemplo com SMTP (envio de email):**  
    ```sh  
    telnet servidor_smtp 25  
    ```  
    Comandos como `HELO`, `MAIL FROM:`, `RCPT TO:`, `DATA`, e `QUIT` são usados para interagir com o servidor.  
  - **Ataques históricos:** No passado, era possível forjar emails (hoje, servidores têm mais segurança).  

##### **5. Tipos de Conexão**  
- **HTTP 1.0:** Fecha a conexão após cada requisição.  
- **HTTP 1.1, FTP:** Permitem múltiplas requisições em uma mesma conexão (**persistente**).  

##### **6. Moral da História**  
- **Nunca confie em emails sem verificação independente!** (Principalmente se forem absurdos como um "festa da vitória" forjada).  
- **Sockets simplificam a programação de rede**, mas o comportamento depende do protocolo utilizado.  

---  
##### **Exemplo Prático (Cliente Socket Simples)**  

```java  
try (Socket socket = new Socket("example.com", 80);  
     OutputStream out = socket.getOutputStream();  
     InputStream in = socket.getInputStream()) {  

    // Envia uma requisição HTTP GET  
    String request = "GET / HTTP/1.1\r\nHost: example.com\r\n\r\n";  
    out.write(request.getBytes());  

    // Lê a resposta  
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));  
    String line;  
    while ((line = reader.readLine()) != null) {  
        System.out.println(line);  
    }  
} catch (IOException e) {  
    e.printStackTrace();  
}  
```  
**Saída:** Mostra o cabeçalho e conteúdo da resposta HTTP do servidor.  

---  
##### **Conclusão**  

- **Sockets são a base da comunicação em redes**, abstraindo detalhes complexos de pacotes.  
- **Telnet ajuda a entender protocolos** antes de implementá-los em código.  
- **Java oferece classes intuitivas** (`Socket`, `ServerSocket`) para criar clientes/servidores.



#### Reading from Servers with Sockets

###### **1. Exemplo Prático: Protocolo Daytime (RFC 867)**  
- **Objetivo:** Conectar ao servidor **time.nist.gov** (porta 13) para obter a hora atual em formato legível.  
- **Teste com Telnet:**  
  ```sh  
  telnet time.nist.gov 13  
  ```  
  Saída (exemplo):  
  ```  
  56375 13-03-24 13:37:50 50 0 0 888.8 UTC(NIST) *  
  ```  
  - **Formato NIST:**  
    - `JJJJJ`: Data Juliana Modificada (dias desde 17/11/1858).  
    - `YY-MM-DD HH:MM:SS`: Data/hora em UTC.  
    - `TT`: Tipo de horário (00 = padrão, 50 = horário de verão).  
    - `L`: Indica ajuste de segundo bissexto (0 = não, 1 = adicionar, 2 = subtrair).  
    - `H`: Saúde do servidor (0 = saudável, 4 = em manutenção).  
    - `msADV`: Atraso estimado da rede (ex: 888.8 ms).  

###### **2. Implementação em Java**  
- **Passos:**  
  1. **Conectar ao servidor:**  
     ```java  
     try (Socket socket = new Socket("time.nist.gov", 13)) {  
         // Configurar timeout (15 segundos)  
         socket.setSoTimeout(15000);  
     } catch (IOException e) {  
         System.err.println("Falha na conexão: " + e.getMessage());  
     }  
     ```  
  2. **Ler dados do servidor:**  
     ```java  
     InputStream in = socket.getInputStream();  
     StringBuilder time = new StringBuilder();  
     InputStreamReader reader = new InputStreamReader(in, "ASCII");  
     int c;  
     while ((c = reader.read()) != -1) {  
         time.append((char) c);  
     }  
     System.out.println(time);  
     ```  

###### **3. Exemplo Completo (Cliente Daytime)**  

```java  
import java.net.*;  
import java.io.*;  

public class DaytimeClient {  
    public static void main(String[] args) {  
        String hostname = args.length > 0 ? args[0] : "time.nist.gov";  
        try (Socket socket = new Socket(hostname, 13)) {  
            socket.setSoTimeout(15000);  
            InputStream in = socket.getInputStream();  
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));  
            String line;  
            while ((line = reader.readLine()) != null) {  
                System.out.println(line);  
            }  
        } catch (IOException e) {  
            System.err.println("Erro: " + e.getMessage());  
        }  
    }  
}  
```  

###### **4. Protocolo Time (RFC 868) - Dados Binários**  
- **Diferença chave:** O servidor envia um número binário de **32 bits** (big-endian) representando segundos desde 01/01/1900 (UTC).  
- **Conversão para `java.util.Date`:**  
  ```java  
  long secondsSince1900 = 0;  
  for (int i = 0; i < 4; i++) {  
      secondsSince1900 = (secondsSince1900 << 8) | raw.read(); // Lê 4 bytes  
  }  
  long secondsSince1970 = secondsSince1900 - 2208988800L; // Conversão para época Unix (1970)  
  Date date = new Date(secondsSince1970 * 1000);  
  ```  

###### **5. Desafios Comuns**  
- **Timeout:** Configurar `setSoTimeout()` evita bloqueios em servidores lentos.  
- **Protocolos não-texto:** Requer leitura de bytes brutos e manipulação com operadores bitwise (ex: `<<`, `|`).  
- **Compatibilidade:** Java não tem tipos unsigned, exigindo ajustes para números grandes.  

###### **6. Comparação entre Protocolos**  
| **Protocolo**     | **Porta** | **Formato Resposta**         | **Uso em Java**                 |     |
| ----------------- | --------- | ---------------------------- | ------------------------------- | --- |
| Daytime (RFC 867) | 13        | Texto (ASCII)                | `Reader.readLine()`             |     |
| Time (RFC 868)    | 37        | Binário (32 bits big-endian) | Leitura byte a byte + conversão |     |

###### **7. Observações Finais**  
- **NIST recomenda migrar para NTP (RFC 5905)** para maior precisão.  
- **Tratamento de erros:** Sempre use `try-with-resources` (Java 7+) ou feche sockets manualmente em `finally`.  
- **Para protocolos complexos,** teste com ferramentas como Telnet antes de implementar.  

##### **Exemplo de Saída**  
```  
56375 13-03-24 15:05:42 50 0 0 843.6 UTC(NIST) *  
```  
Ou, para o protocolo Time:  
```  
It is Sun Mar 24 12:22:17 EDT 2013  
```  


##### **Conclusão**  

- **Sockets simplificam a comunicação**, mas o formato dos dados varia conforme o protocolo.  
- **Daytime** é simples (texto ASCII), enquanto **Time** exige manipulação de bytes.  
- **Sempre consulte a RFC** do protocolo para entender o formato esperado.



#### Writing to Servers with Sockets

##### **1. Comunicação Bidirecional com Sockets**  
- **Leitura e escrita:** Um socket permite obter **InputStream** (para ler dados) e **OutputStream** (para enviar dados).  
- **Padrão comum:**  
  - O **cliente envia uma requisição** (ex: comando).  
  - O **servidor responde**.  
  - Repete-se até que uma das partes feche a conexão.  

##### **2. Exemplo: Protocolo DICT (RFC 2229)**  
- **Objetivo:** Traduzir palavras usando um servidor DICT (ex: `dict.org`, porta **2628**).  
- **Fluxo típico:**  
  1. Cliente conecta-se ao servidor.  
  2. Envia comandos como `DEFINE dicionário palavra` (ex: `DEFINE eng-lat gold`).  
  3. Servidor retorna definições em texto, terminando com um ponto (`.`) em uma linha separada.  
  4. Cliente envia `quit` para finalizar.  

##### **3. Implementação em Java**  
- **Passos principais:**  
  1. **Conectar ao servidor:**  
     ```java  
     Socket socket = new Socket("dict.org", 2628);  
     socket.setSoTimeout(15000); // Timeout de 15 segundos  
     ```  
  2. **Preparar escritor (envio):**  
     ```java  
     OutputStream out = socket.getOutputStream();  
     Writer writer = new OutputStreamWriter(out, "UTF-8");  
     writer = new BufferedWriter(writer); // Melhora desempenho  
     ```  
  3. **Enviar comandos:**  
     ```java  
     writer.write("DEFINE eng-lat gold\r\n");  
     writer.flush(); // Força envio imediato  
     ```  
  4. **Ler respostas:**  
     ```java  
     InputStream in = socket.getInputStream();  
     BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));  
     String line;  
     while ((line = reader.readLine()) != null) {  
         if (line.equals(".")) break; // Fim da definição  
         System.out.println(line);  
     }  
     ```  
  5. **Finalizar conexão:**  
     ```java  
     writer.write("quit\r\n");  
     writer.flush();  
     socket.close();  
     ```  

##### **4. Exemplo Completo (Cliente DICT)**  
```java  
import java.io.*;  
import java.net.*;  

public class TradutorInglesLatim {  
    public static void main(String[] args) {  
        try (Socket socket = new Socket("dict.org", 2628)) {  
            socket.setSoTimeout(15000);  
            Writer writer = new BufferedWriter(  
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));  
            BufferedReader reader = new BufferedReader(  
                new InputStreamReader(socket.getInputStream(), "UTF-8"));  

            for (String palavra : args) {  
                writer.write("DEFINE eng-lat " + palavra + "\r\n");  
                writer.flush();  
                String linha;  
                while ((linha = reader.readLine()) != null) {  
                    if (linha.startsWith("250 ")) break; // OK  
                    if (linha.startsWith("552 ")) {  
                        System.out.println("Palavra não encontrada: " + palavra);  
                        break;  
                    }  
                    if (!linha.matches("\\d\\d\\d .*") && !linha.trim().equals(".")) {  
                        System.out.println(linha);  
                    }  
                }  
            }  
            writer.write("quit\r\n");  
            writer.flush();  
        } catch (IOException e) {  
            System.err.println("Erro: " + e.getMessage());  
        }  
    }  
}  
```  

##### **5. Execução do Programa**  
- **Entrada:**  
  ```sh  
  java TradutorInglesLatim gold silver uranium  
  ```  
- **Saída (exemplo):**  
  ```  
  gold [gould]  
   aurarius; aureus; chryseus  
   aurum; chrysos  
  silver [silvər]  
   argenteus  
   argentum  
  Palavra não encontrada: uranium  
  ```  

##### **6. Observações Importantes**  
- **Protocolos baseados em texto:** Usam `Writer` (para envio) e `Reader` (para leitura) com codificação adequada (ex: UTF-8).  
- **Controle de fluxo:**  
  - `flush()` garante que os dados sejam enviados imediatamente.  
  - Linhas terminam com `\r\n` (padrão em muitos protocolos).  
- **Tratamento de erros:** Verifique códigos de resposta (ex: `552 no match` para palavras desconhecidas).  

##### **7. Comparação com Leitura (Daytime/Time)**  
| **Operação** | **Daytime/Time**                  | **DICT**                     |     |
| ------------ | --------------------------------- | ---------------------------- | --- |
| **Direção**  | Apenas leitura                    | Leitura e escrita            |     |
| **Formato**  | Texto (Daytime) ou binário (Time) | Texto (comandos + respostas) |     |
| **Exemplo**  | `time.nist.gov` (porta 13/37)     | `dict.org` (porta 2628)      |     |

##### **Conclusão**  
- Escrever em servidores via sockets segue o mesmo princípio de leitura, mas exige **coordenação entre envio e recebimento** conforme o protocolo.  
- O exemplo DICT ilustra um **protocolo bidirecional simples**, mas outros (como HTTP POST) podem ser mais complexos.  
- Sempre **consulte a RFC** do protocolo para entender os comandos e formatos de resposta.


#### Half-closed sockets


##### **1. Conceito de Sockets Semi-Fechados**  
- Um socket pode ter sua comunicação **parcialmente encerrada**:  
  - **`shutdownInput()`**: Fecha apenas o fluxo de **entrada** (leitura).  
  - **`shutdownOutput()`**: Fecha apenas o fluxo de **saída** (escrita).  
- **Não libera recursos do socket** (como portas). O socket ainda precisa ser fechado com `close()` após o uso.  

##### **2. Quando Usar?**  
- **Protocolos unidirecionais após requisição**:  
  - Exemplo: Envia uma requisição HTTP e depois só lê a resposta.  
  - Melhora eficiência, sinalizando ao servidor que o cliente terminou de enviar dados.  

##### **3. Exemplo Prático (HTTP)**  
```java  
try (Socket conexao = new Socket("www.oreilly.com", 80)) {  
    Writer out = new OutputStreamWriter(conexao.getOutputStream(), "8859_1");  
    out.write("GET / HTTP/1.0\r\n\r\n");  // Envia requisição  
    out.flush();  
    conexao.shutdownOutput();  // Fecha o lado de escrita (não enviará mais nada)  

    // Lê a resposta do servidor...  
    BufferedReader in = new BufferedReader(  
        new InputStreamReader(conexao.getInputStream()));  
    String linha;  
    while ((linha = in.readLine()) != null) {  
        System.out.println(linha);  
    }  
} catch (IOException ex) {  
    ex.printStackTrace();  
}  
```  

##### **4. Comportamento dos Fluxos**  
- **Após `shutdownInput()`**:  
  - Tentativas de leitura retornam `-1` (fim do fluxo).  
- **Após `shutdownOutput()`**:  
  - Tentativas de escrita lançam `IOException`.  

##### **5. Métodos de Verificação**  
- **`isInputShutdown()`**: Retorna `true` se a entrada foi fechada.  
- **`isOutputShutdown()`**: Retorna `true` se a saída foi fechada.  
- Úteis para verificar o estado do socket sem fechá-lo completamente.  

##### **6. Comparação com `close()`**  
| **Método**         | **Efeito**                                                                |
| ------------------ | ------------------------------------------------------------------------- |
| `shutdownInput()`  | Sinaliza fim da leitura (não libera recursos do socket).                  |
| `shutdownOutput()` | Sinaliza fim da escrita (útil para protocolos como HTTP após requisição). |
| `close()`          | Fecha totalmente o socket e libera recursos.                              |

##### **7. Cuidados Importantes**  
- **Sempre feche o socket** com `close()` após o uso, mesmo usando `shutdownInput()`/`shutdownOutput()`.  
- **Protocolos como HTTP/1.0** encerram a conexão após uma requisição, tornando `shutdownOutput()` útil para otimização.  

---  
##### **Exemplo de Aplicação**  

**Cenário**: Cliente envia uma consulta WHOIS e só espera a resposta:  
```java  
Socket socket = new Socket("whois.example.com", 43);  
Writer out = new OutputStreamWriter(socket.getOutputStream());  
out.write("example.com\r\n");  
out.flush();  
socket.shutdownOutput();  // Indica que não enviará mais dados  

// Lê a resposta...  
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
String resposta;  
while ((resposta = in.readLine()) != null) {  
    System.out.println(resposta);  
}  
socket.close();  // Libera recursos  
```  


- **Sockets semi-fechados** são úteis para otimizar comunicação em protocolos com etapas bem definidas (envio → recebimento).  
- Use `shutdownOutput()` para indicar ao servidor que o cliente concluiu o envio de dados.  
- Combine com `isInputShutdown()`/`isOutputShutdown()` para controle preciso do estado da conexão.


#### Constructing and Connecting Sockets


A classe `java.net.Socket` é a base para operações TCP do lado do cliente em Java. Ela é usada por outras classes (como `URL`, `URLConnection`, etc.) para estabelecer conexões de rede.  

---  

##### **1. Construtores Básicos**  

Cria um socket **já conectado** a um host e porta remotos:  
```java  
public Socket(String host, int port) throws UnknownHostException, IOException  
public Socket(InetAddress host, int port) throws IOException  
```  
- **Exemplo:**  
  ```java  
  try {  
      Socket socket = new Socket("www.oreilly.com", 80); // Conecta ao servidor HTTP  
      // Envia/recebe dados...  
  } catch (UnknownHostException e) {  
      System.err.println("Host não encontrado: " + e.getMessage());  
  } catch (IOException e) {  
      System.err.println("Erro de conexão: " + e.getMessage());  
  }  
  ```  
- **Possíveis erros:**  
  - `UnknownHostException`: O DNS não resolveu o host.  
  - `IOException`: Falha na conexão (ex.: porta bloqueada, rede indisponível).  

---  

##### **2. Verificação de Portas Abertas**  

É possível usar o `Socket` para verificar quais portas em um host estão aceitando conexões:  
```java  
for (int porta = 1; porta < 1024; porta++) {  
    try {  
        Socket socket = new Socket("localhost", porta);  
        System.out.println("Servidor encontrado na porta: " + porta);  
        socket.close();  
    } catch (IOException e) {  
        // Porta provavelmente fechada  
    }  
}  
```  
**Saída (exemplo):**  
```  
Servidor encontrado na porta: 21 (FTP)  
Servidor encontrado na porta: 22 (SSH)  
Servidor encontrado na porta: 80 (HTTP)  
```  

---  

##### **3. Construtores para Sockets Não Conectados**  

Criam um socket **sem conexão automática**, permitindo configurações avançadas (proxy, criptografia, etc.):  
```java  
public Socket()                     // Socket não conectado  
public Socket(Proxy proxy)          // Usa um proxy configurado  
protected Socket(SocketImpl impl)   // Personalização avançada (raro)  
```  
- **Uso típico:**  
  - Configurar opções (ex.: timeout) antes da conexão.  
  - Conectar manualmente com `connect()`.  

---  

##### **Exemplo Completo (Scanner de Portas)**  

```java  
import java.net.*;  
import java.io.*;  

public class ScannerDePortas {  
    public static void main(String[] args) {  
        String host = args.length > 0 ? args[0] : "localhost";  
        for (int porta = 1; porta < 1024; porta++) {  
            try (Socket socket = new Socket(host, porta)) {  
                System.out.println("✅ Porta " + porta + " aberta em " + host);  
            } catch (IOException e) {  
                // Porta fechada ou erro de conexão  
            }  
        }  
    }  
}  
```  

---  

##### **Casos de Uso**  

- **Teste de segurança:** Identificar portas abertas não autorizadas.  
- **Desenvolvimento:** Verificar se um serviço (ex.: banco de dados) está acessível.  
- **Depuração:** Diagnosticar problemas de conectividade.  


##### **Observações Importantes**  
1. **Sempre feche o socket** (usando `try-with-resources` ou `finally`).  
2. **Evite scanners agressivos** em redes desconhecidas (pode ser considerado um ataque).  
3. **Para serviços conhecidos**, consulte `/etc/services` (Unix) ou documentação oficial.  

##### **Conclusão**

- O `Socket` é a classe central para comunicação TCP em Java.  
- Construtores básicos simplificam a conexão, enquanto opções avançadas permitem maior controle.  
- Use scanners de porta com responsabilidade para diagnóstico e segurança.


#### Picking a Local Interface to Connect From

A classe `Socket` em Java permite especificar **não apenas o destino da conexão** (host e porta remotos), mas também **a interface de rede local** e a porta de origem a serem usadas. Isso é útil em cenários onde você precisa controlar por qual interface de rede o tráfego será enviado.

---

##### **Construtores para Conexão com Interface Local Específica**

Dois construtores permitem definir o host/porta de destino **e** a interface/porta de origem:
```java
public Socket(String host, int port, InetAddress interface, int localPort)  
    throws IOException, UnknownHostException  

public Socket(InetAddress host, int port, InetAddress interface, int localPort)  
    throws IOException
```

##### **Parâmetros:**

- **`host` + `port`**: Destino da conexão (ex: `"mail"` na porta `25` para SMTP).
- **`interface`**: Interface local (ex: um endereço IP específico da máquina).
- **`localPort`**: Porta de origem. Se for `0`, o Java escolhe uma porta aleatória entre **1024 e 65535**.

---

##### **Casos de Uso Comuns**

1. **Roteadores/Firewalls com Múltiplas Interfaces**  
   - Exemplo: Um roteador com duas portas Ethernet:
     - **Interface externa (`eth0`)**: Recebe tráfego da Internet.
     - **Interface interna (`eth1`)**: Encaminha tráfego para a rede local.
   - Se um programa precisa enviar logs para um servidor interno, deve usar a interface interna para evitar rotas desnecessárias.

2. **Restrição de Licença de Software**  
   - Um programa pode ser configurado para só funcionar se conseguir se vincular a um endereço IP específico da máquina (ex: `InetAddress.getByName("servidor-licenca")`).  
   - *Observação:* Isso não é totalmente seguro, pois Java pode ser descompilado.

---

##### **Exemplo Prático**
```java
try {
    // Define a interface local (ex: "router" para a interface interna)
    InetAddress interfaceLocal = InetAddress.getByName("192.168.1.100");
    
    // Conecta ao servidor de email (porta 25) usando a interface específica
    Socket socket = new Socket("mail.servidor.com", 25, interfaceLocal, 0);
    
    // Trabalha com o socket...
    System.out.println("Conectado de: " + socket.getLocalAddress() + ":" + socket.getLocalPort());
    
} catch (UnknownHostException e) {
    System.err.println("Host desconhecido: " + e.getMessage());
} catch (IOException e) {
    System.err.println("Erro de conexão: " + e.getMessage());
}
```

##### **Possíveis Erros:**
- **`UnknownHostException`**: Se o host de destino ou a interface local não for resolvida.
- **`IOException`** (ou `BindException`): Se o socket não puder se vincular à interface local (ex: programa rodando em `maquinaA` tentando usar o IP de `maquinaB`).

---

##### **Detalhes Importantes**

1. **Porta `0`**:  
   - O sistema escolhe automaticamente uma porta disponível (útil para evitar conflitos).

2. **Interfaces de Rede**:  
   - Podem ser físicas (placa de rede) ou virtuais (vários IPs em uma mesma máquina).

3. **Segurança**:  
   - Restringir conexões a uma interface específica pode ser parte de um esquema de licenciamento, mas **não é à prova de falhas** devido à facilidade de engenharia reversa em Java.

---

##### **Quando Usar?**

- Em máquinas **multi-homed** (com múltiplos endereços IP).
- Para **roteamento específico** (ex: tráfego interno vs. externo).
- Em testes de rede onde você precisa simular conexões de origens distintas.

---

##### **Conclusão**

Esses construtores oferecem controle avançado sobre conexões de rede, permitindo definir **exatamente de onde o tráfego deve sair**. Embora seu uso seja menos comum, é essencial em cenários de roteamento complexo ou restrições de infraestrutura.


#### Constructing Without Connecting

A classe `Socket` em Java permite **criar um objeto de socket sem estabelecer uma conexão imediata**, o que oferece maior flexibilidade para configurar opções antes da conexão ou para melhorar a organização do código.  

---

##### **Construtor Básico sem Conexão**  
```java  
public Socket()  
```  
- Cria um socket **não conectado**.  
- Útil quando você precisa configurar opções (como timeout) antes de conectar.  

---

##### **Estabelecendo a Conexão Posteriormente**  
Use o método `connect()` para conectar o socket a um endereço remoto (`SocketAddress`):  
```java  
try {  
    Socket socket = new Socket(); // Socket não conectado  
    SocketAddress endereco = new InetSocketAddress("time.nist.gov", 13);  
    socket.connect(endereco); // Conecta ao servidor  
    // Trabalha com o socket...  
} catch (IOException ex) {  
    System.err.println("Erro: " + ex.getMessage());  
}  
```  

##### **Timeout de Conexão**  
Você pode definir um tempo máximo de espera (em milissegundos):  
```java  
socket.connect(endereco, 5000); // Timeout de 5 segundos  
```  
- Se `timeout = 0` (padrão), a conexão espera indefinidamente.  

---

##### **Vantagens do Construtor sem Conexão**  
1. **Configuração Antecipada de Opções**  
   - Algumas opções de socket só podem ser definidas **antes da conexão** (ex.: `SO_RCVBUF`).  

2. **Melhoria na Organização do Código**  
   - Evita verificações redundantes de `null` em blocos `try-catch-finally` (especialmente em Java 6 e versões anteriores).  

##### **Exemplo: Comparação de Abordagens**  

**Antes (construtor tradicional):**  
```java  
Socket socket = null;  
try {  
    socket = new Socket("servidor", 80); // Conexão imediata  
    // Trabalha com o socket...  
} finally {  
    if (socket != null) {  
        try { socket.close(); } catch (IOException ex) { /* ignora */ }  
    }  
}  
```  

**Depois (construtor sem conexão):**  
```java  
Socket socket = new Socket(); // Sem exceções aqui  
try {  
    socket.connect(new InetSocketAddress("servidor", 80));  
    // Trabalha com o socket...  
} finally {  
    try { socket.close(); } catch (IOException ex) { /* ignora */ }  
}  
```  
- Elimina a necessidade de verificar `socket != null`.  

---

##### **Cenários de Uso**  
- **Configuração de Timeout**: Definir `SO_TIMEOUT` antes de conectar.  
- **Controle de Buffer**: Ajustar tamanhos de buffer de rede.  
- **Código Mais Limpo**: Evitar aninhamento excessivo em blocos `try-catch`.  

---

##### **Observações**  
- **Java 7+**: Prefira `try-with-resources` para gerenciamento automático de recursos:  
  ```java  
  try (Socket socket = new Socket()) {  
      socket.connect(new InetSocketAddress("servidor", 80));  
      // Trabalha com o socket...  
  } catch (IOException ex) {  
      System.err.println("Erro: " + ex.getMessage());  
  }  
  ```  
- **Exceções**: `connect()` pode lançar `IOException` se a conexão falhar ou o timeout expirar.  

---

##### **Conclusão**  

O construtor `Socket()` sem argumentos é útil para:  
1. **Postergar a conexão** até que opções sejam configuradas.  
2. **Escrever código mais legível** sem verificações desnecessárias de `null`.  
3. **Evitar erros** em cenários onde a conexão imediata não é desejada.  

Use-o quando precisar de controle preciso sobre o processo de conexão ou para simplificar estruturas de tratamento de erros.


#### Socket Addresses

A classe `SocketAddress` representa um **ponto de conexão** (endpoint) para sockets. Embora seja uma classe abstrata sem métodos próprios, ela serve como base para armazenar informações de conexão, como **IP e porta**. Na prática, a única implementação usada no JDK é `InetSocketAddress`, voltada para sockets TCP/IP.  

##### **Principais Funcionalidades**  

###### **1. Obtenção de Endereços**  
Um `Socket` pode retornar dois tipos de endereços:  
- **`getRemoteSocketAddress()`**: Retorna o endereço do host remoto (ex: `www.google.com:80`).  
- **`getLocalSocketAddress()`**: Retorna o endereço local (IP + porta) usado na conexão.  

**Exemplo:**  
```java  
Socket socket = new Socket("www.google.com", 80);  

// Endereço do Google  
SocketAddress enderecoRemoto = socket.getRemoteSocketAddress();  

// Endereço local (ex: 192.168.1.100:54321)  
SocketAddress enderecoLocal = socket.getLocalSocketAddress();  

socket.close();  

// Reconecta usando o endereço salvo  
Socket novo = new Socket();  
novo.connect(enderecoRemoto);  
```  

###### **2. Classe `InetSocketAddress`**  
É a implementação concreta de `SocketAddress` e pode ser criada de três formas:  
1. **Com `InetAddress` + porta**:  
   ```java  
   InetAddress ip = InetAddress.getByName("google.com");  
   InetSocketAddress endereco = new InetSocketAddress(ip, 80);  
   ```  
2. **Com hostname (String) + porta**:  
   ```java  
   InetSocketAddress endereco = new InetSocketAddress("google.com", 80);  
   ```  
3. **Apenas porta (para servidores)**:  
   ```java  
   InetSocketAddress endereco = new InetSocketAddress(8080); // Escuta em 0.0.0.0:8080  
   ```  

**Métodos úteis:**  
- `getAddress()`: Retorna o `InetAddress` (IP).  
- `getPort()`: Retorna a porta.  
- `getHostName()`: Retorna o hostname (ex: `"google.com"`).  

###### **3. Pular Resolução DNS**  
Se você já tem o hostname e não quer uma consulta DNS, use:  
```java  
InetSocketAddress enderecoNaoResolvido = InetSocketAddress.createUnresolved("google.com", 80);  
```  

---

##### **Casos de Uso**  
- **Reconexão rápida**: Salvar `SocketAddress` para reconectar sem resolver DNS novamente.  
- **Logs e auditoria**: Registrar endereços locais/remotos para rastreamento.  
- **Servidores**: Criar `InetSocketAddress` com apenas uma porta para escutar em todas as interfaces.  

---

##### **Exemplo Completo**  

```java  
import java.net.*;  

public class ExemploEnderecoSocket {  
    public static void main(String[] args) throws IOException {  
        // Conecta ao Google  
        Socket socket = new Socket("www.google.com", 80);  

        // Obtém endereços  
        InetSocketAddress remoto = (InetSocketAddress) socket.getRemoteSocketAddress();  
        System.out.println("Conectado a: " + remoto.getHostName() + ":" + remoto.getPort());  

        InetSocketAddress local = (InetSocketAddress) socket.getLocalSocketAddress();  
        System.out.println("Endereço local: " + local.getAddress() + ":" + local.getPort());  

        socket.close();  

        // Reconecta usando o endereço salvo  
        Socket novo = new Socket();  
        novo.connect(remoto);  
        System.out.println("Reconectado!");  
        novo.close();  
    }  
}  
```  

**Saída:**  
```  
Conectado a: www.google.com:80  
Endereço local: 192.168.1.100:54321  
Reconectado!  
```  

---

##### **Observações**  
- **Retorno `null`**: Se o socket não estiver conectado, `getRemoteSocketAddress()` e `getLocalSocketAddress()` retornam `null`.  
- **UDP**: Embora `SocketAddress` seja genérico, no Java atual só é usado para TCP/IP.  

---

##### **Conclusão**  

`SocketAddress` e `InetSocketAddress` são úteis para:  
✅ **Armazenar endpoints** para reconexão eficiente.  
✅ **Identificar conexões** (local/remoto) em logs.  
✅ **Configurar servidores** que escutam em portas específicas.  

Use `InetSocketAddress` sempre que precisar de um endereço de socket concreto em Java.


#### Proxy Servers


A classe `Socket` em Java permite configurar conexões que passam por um **servidor proxy**, ignorando as configurações padrão do sistema. Isso é útil para cenários onde é necessário controlar manualmente o roteamento do tráfego de rede.  

---  

##### **Construtor para Uso com Proxy**  

```java  
public Socket(Proxy proxy)  
```  
- Cria um socket **não conectado** que usará o proxy especificado.  
- Se `proxy = Proxy.NO_PROXY`, o socket ignora proxies configurados no sistema e tenta uma conexão direta (útil para contornar firewalls, se permitido).  

---  

##### **Tipos de Proxy Suportados**  

Java reconhece três tipos de proxy:  
1. **`Proxy.Type.SOCKS`** (nível de transporte):  
   - Usado para proxies SOCKS (ex: `SOCKS4`, `SOCKS5`).  
   - **Exemplo:**  
     ```java  
     SocketAddress proxyAddress = new InetSocketAddress("meuproxy.com", 1080);  
     Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddress);  
     Socket socket = new Socket(proxy);  
     socket.connect(new InetSocketAddress("exemplo.com", 80));  
     ```  

2. **`Proxy.Type.HTTP`** (nível de aplicação):  
   - Para proxies HTTP (como Squid ou Nginx).  
   - *Observação:* Requer configuração adicional (geralmente via `Authenticator`).  

3. **`Proxy.Type.DIRECT`** (sem proxy):  
   - Equivalente a `Proxy.NO_PROXY`.  

---  

##### **Exemplo Prático (SOCKS Proxy)**  
```java  
import java.net.*;  

public class ExemploProxy {  
    public static void main(String[] args) throws IOException {  
        // Configura o proxy SOCKS  
        SocketAddress enderecoProxy = new InetSocketAddress("proxy.example.com", 1080);  
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, enderecoProxy);  

        // Cria o socket com o proxy  
        try (Socket socket = new Socket(proxy)) {  
            // Conecta ao destino final  
            SocketAddress destino = new InetSocketAddress("ibiblio.org", 25);  
            socket.connect(destino);  
            System.out.println("Conectado via proxy!");  
        }  
    }  
}  
```  

---  

##### **Cenários de Uso**  
- **Acesso restrito:** Contornar políticas de rede corporativa (se autorizado).  
- **Testes:** Simular conexões a partir de diferentes localizações.  
- **Anonimato:** Rotear tráfego através de proxies externos (com cautela).  

---  

##### **Limitações**  
- **Firewalls:** Se um firewall bloquear conexões diretas, mesmo `Proxy.NO_PROXY` falhará.  
- **Autenticação:** Proxies HTTP/SOCKS podem exigir credenciais (use `Authenticator`).  
- **Performance:** Proxies adicionam latência à conexão.  

---  

##### **Comparação com Configuração Global**  
| **Abordagem**               | **Vantagem**                                    | **Desvantagem**     |
| --------------------------- | ----------------------------------------------- | ------------------- |
| **`Socket(Proxy)`**         | Controle por conexão                            | Configuração manual |
| **Propriedades do Sistema** | Aplicável a todos os sockets (`socksProxyHost`) | Menos flexível      |

---  

##### **Conclusão**  
O construtor `Socket(Proxy)` é ideal quando você precisa:  
✅ **Escolher um proxy específico** para uma conexão individual.  
✅ **Ignorar proxies** globais (`NO_PROXY`).  
✅ **Trabalhar com SOCKS** (o único tipo de proxy de baixo nível suportado diretamente).  

Use-o para aplicações que exigem controle granular sobre o roteamento de rede, como ferramentas de scraping ou clientes de e-mail em redes restritas.




#### Getting Information About a Socket


A classe `Socket` em Java fornece métodos para acessar informações sobre a conexão, como **endereços e portas** (local e remoto). Esses dados são úteis para depuração, logs ou controle de fluxo em aplicações de rede.  

---

##### **Métodos Principais**  


| **Método**                | **Descrição**                                                                 |
|---------------------------|-------------------------------------------------------------------------------|
| `getInetAddress()`        | Retorna o **endereço remoto** (ex: `www.google.com`).                         |
| `getPort()`               | Retorna a **porta remota** (ex: `80` para HTTP).                              |
| `getLocalAddress()`       | Retorna o **endereço local** (IP da máquina que iniciou a conexão).           |
| `getLocalPort()`          | Retorna a **porta local** (aleatória, escolhida pelo sistema durante a conexão). |

---

##### **Características Importantes**  

1. **Porta Local**:  
   - É atribuída dinamicamente pelo sistema (geralmente entre **49152 e 65535**).  
   - Permite múltiplas conexões simultâneas de um mesmo cliente.  

2. **Porta Remota**:  
   - Normalmente é uma **porta conhecida** (ex: `80` para HTTP, `25` para SMTP).  

3. **Sem Setters**:  
   - Essas propriedades são definidas durante a conexão e **não podem ser alteradas**.  

---

##### **Exemplo Prático**  

O código abaixo conecta-se a hosts listados na linha de comando e exibe informações da conexão:  

```java  
import java.net.*;  
import java.io.*;  

public class InfoSocket {  
    public static void main(String[] args) {  
        for (String host : args) {  
            try (Socket socket = new Socket(host, 80)) {  
                System.out.println("Conectado a " + socket.getInetAddress()  
                    + " na porta " + socket.getPort()  
                    + " a partir da porta " + socket.getLocalPort()  
                    + " do endereço " + socket.getLocalAddress());  
            } catch (UnknownHostException ex) {  
                System.err.println("Host não encontrado: " + host);  
            } catch (IOException ex) {  
                System.err.println("Erro ao conectar a " + host + ": " + ex.getMessage());  
            }  
        }  
    }  
}  
```  

**Saída de Exemplo:**  
```  
Conectado a www.oreilly.com/208.201.239.37 na porta 80 a partir da porta 49156 do endereço /192.168.1.100  
Conectado a www.google.com/172.217.0.46 na porta 80 a partir da porta 49157 do endereço /192.168.1.100  
Erro ao conectar a login.ibiblio.org: Connection refused  
```  

---

##### **Explicação da Saída**  

- www.oreilly.com:  
  - **Remoto**: IP `208.201.239.37`, porta `80`.  
  - **Local**: IP `192.168.1.100`, porta aleatória `49156`.  
- **login.ibiblio.org**: Falha porque não há servidor na porta `80`.  

---

##### **Casos de Uso**  

- **Depuração**: Identificar falhas de conexão (ex: porta errada ou host inacessível).  
- **Logs**: Registrar origens/destinos de conexões para auditoria.  
- **Balanceamento de Carga**: Distribuir tráfego com base em portas locais.  

---

##### **Observações**  

- **Conexões Fechadas**: Os métodos retornam os dados da última conexão válida.  
- **IPv6/IPv4**: Os endereços podem ser em qualquer formato (ex: `2001:db8::1`).  

---

##### **Conclusão**  

Esses métodos fornecem **detalhes essenciais** sobre conexões de rede em Java, permitindo:  
✅ **Rastrear** o caminho do tráfego.  
✅ **Diagnosticar** problemas de conectividade.  
✅ **Documentar** atividades de rede em aplicações.  

Use-os para tornar suas aplicações de socket mais transparentes e robustas.

#### Closed or Connected?

A classe `Socket` em Java oferece métodos para verificar o estado de uma conexão, mas alguns têm comportamentos sutis que podem causar confusão.  

---

##### **Métodos de Verificação**  

| **Método**         | **Descrição**                                                                 | **Observações**                                                                 |
|--------------------|-------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| `isClosed()`       | Retorna `true` se o socket foi **fechado** explicitamente (com `close()`).    | Retorna `false` se o socket nunca foi conectado (mesmo que não esteja "aberto"). |
| `isConnected()`    | Retorna `true` se o socket **já foi conectado** a um host remoto.             | Continua retornando `true` mesmo após o socket ser fechado.                      |
| `isBound()`        | Retorna `true` se o socket foi **vinculado** a uma porta local.               | Importante para sockets de servidor (ver Capítulo 9).                            |

---

##### **Como Verificar se um Socket Está Ativo?**  
Para saber se um socket está **realmente conectado e aberto**, combine os métodos:  
```java  
boolean estaAtivo = socket.isConnected() && !socket.isClosed();  
```  

**Exemplo de Uso:**  
```java  
if (socket.isConnected() && !socket.isClosed()) {  
    System.out.println("Socket ativo! Pronto para enviar/receber dados.");  
} else {  
    System.err.println("Socket não está disponível.");  
}  
```  

---

##### **Método `toString()`**  
Retorna uma representação em String do socket no formato:  
```  
Socket[addr=www.oreilly.com/198.112.208.11, port=80, localport=50055]  
```  
- **Uso principal:** Depuração (não confie no formato para lógica de programa).  
- **Equivalente a:**  
  - `addr` → `getInetAddress()`.  
  - `port` → `getPort()`.  
  - `localport` → `getLocalPort()`.  

---

##### **Comparação entre Sockets**  
- **`equals()` e `hashCode()`** **não são sobrescritos** pela classe `Socket`.  
- Dois sockets são considerados iguais **apenas se forem o mesmo objeto** (comparação de referência).  

**Exemplo:**  
```java  
Socket socket1 = new Socket("example.com", 80);  
Socket socket2 = socket1;  

System.out.println(socket1.equals(socket2)); // true (mesmo objeto)  
System.out.println(socket1.equals(new Socket("example.com", 80))); // false (objetos diferentes)  
```  

---

##### **Quando Usar?**  
- **`isClosed()`:** Para evitar operações em sockets já fechados.  
- **`isConnected()` + `isClosed()`:** Para confirmar se a conexão está ativa.  
- **`isBound()`:** Útil em sockets de servidor (vinculados a portas locais).  

---

##### **Exemplo Completo**  
```java  
import java.net.*;  

public class EstadoSocket {  
    public static void main(String[] args) throws IOException {  
        try (Socket socket = new Socket("www.google.com", 80)) {  
            System.out.println("Estado inicial:");  
            System.out.println("isConnected(): " + socket.isConnected()); // true  
            System.out.println("isClosed(): " + socket.isClosed());      // false  
            System.out.println("isBound(): " + socket.isBound());        // true  

            System.out.println("\nApós fechar:");  
            socket.close();  
            System.out.println("isConnected(): " + socket.isConnected()); // true (!)  
            System.out.println("isClosed(): " + socket.isClosed());        // true  
        }  
    }  
}  
```  

**Saída:**  
```  
Estado inicial:  
isConnected(): true  
isClosed(): false  
isBound(): true  

Após fechar:  
isConnected(): true  
isClosed(): true  
```  

---

##### **Conclusão**  
- **`isConnected()`** indica histórico (se o socket já conectou).  
- **`isClosed()`** indica se foi explicitamente fechado.  
- Combine ambos para verificar conexões ativas.  
- Use `toString()` apenas para logs/debug.  
- Evite comparar sockets diretamente (use referências).


#### Setting Socket Options

Os **socket options** são parâmetros que controlam como os sockets enviam e recebem dados. Java oferece suporte a nove opções principais para sockets do lado do cliente, herdadas das convenções de nomes do Unix (C).  

---

##### **Opções Disponíveis**  

| **Opção**         | **Descrição**                                                                 | **Métodos Java**                                  |
|--------------------|-------------------------------------------------------------------------------|--------------------------------------------------|
| **`TCP_NODELAY`** | Desativa o algoritmo de Nagle (envia dados imediatamente, sem buffer).        | `setTcpNoDelay(boolean)` / `getTcpNoDelay()`     |
| **`SO_BINDADDR`** | Define o endereço local para vincular o socket.                               | *(Apenas leitura via `getLocalAddress()`)*       |
| **`SO_TIMEOUT`**  | Tempo máximo (ms) de espera em operações de leitura (`read()`).               | `setSoTimeout(int)` / `getSoTimeout()`           |
| **`SO_LINGER`**   | Tempo de espera para finalizar conexão ao fechar (`close()`).                 | `setSoLinger(boolean, int)` / `getSoLinger()`    |
| **`SO_SNDBUF`**   | Tamanho do buffer de envio (em bytes).                                        | `setSendBufferSize(int)` / `getSendBufferSize()` |
| **`SO_RCVBUF`**   | Tamanho do buffer de recebimento (em bytes).                                  | `setReceiveBufferSize(int)` / `getReceiveBufferSize()` |
| **`SO_KEEPALIVE`** | Habilita "keep-alive" para verificar se a conexão está ativa.                | `setKeepAlive(boolean)` / `getKeepAlive()`       |
| **`OOBINLINE`**   | Habilita recebimento de dados "out-of-band" (urgentes) no fluxo normal.       | `setOOBInline(boolean)` / `getOOBInline()`       |
| **`IP_TOS`**      | Prioridade do tráfego (Quality of Service - QoS).                             | `setTrafficClass(int)` / `getTrafficClass()`     |

---

##### **Exemplos de Uso**  

###### **1. TCP_NODELAY (Nagle Algorithm)**  
Útil para aplicações em tempo real (ex: jogos), onde latência é crítica:  
```java  
Socket socket = new Socket("example.com", 80);  
socket.setTcpNoDelay(true); // Desativa buffering (envia dados imediatamente)  
```  

###### **2. SO_TIMEOUT**  
Evita bloqueio infinito em operações de leitura:  
```java  
socket.setSoTimeout(5000); // Timeout de 5 segundos para read()  
```  

###### **3. SO_LINGER**  
Controla o fechamento gracioso da conexão:  
```java  
socket.setSoLinger(true, 30); // Espera 30 segundos para finalizar  
```  

###### **4. Buffers (SO_SNDBUF e SO_RCVBUF)**  
Ajusta o tamanho dos buffers para otimizar throughput:  
```java  
socket.setSendBufferSize(64 * 1024); // 64 KB para envio  
socket.setReceiveBufferSize(128 * 1024); // 128 KB para recebimento  
```  

---

##### **Detalhes Importantes**  
- **Convenção de Nomes**: As opções seguem padrões do Unix (ex: `SO_` para *Socket Option*).  
- **Só podem ser alteradas antes da conexão?**  
  - Algumas opções (como `SO_RCVBUF`) devem ser definidas **antes** de `connect()`.  
  - Outras (como `SO_TIMEOUT`) podem ser ajustadas a qualquer momento.  
- **Padrões**: O sistema operacional define valores padrão, mas ajustá-los pode melhorar desempenho.  

---

##### **Caso de Uso Avançado (QoS com IP_TOS)**  
Prioriza tráfego para serviços críticos:  
```java  
socket.setTrafficClass(0x10); // Prioridade baixa (0x10) para tráfego padrão  
// Ou 0x08 para prioridade alta (ex: VoIP)  
```  

---

##### **Conclusão**  
As opções de socket permitem:  
✅ **Otimizar desempenho** (buffers, Nagle algorithm).  
✅ **Controlar tempo de espera** (timeouts, linger).  
✅ **Priorizar tráfego** (QoS).  

Use-as para ajustar o comportamento do socket conforme as necessidades da sua aplicação.


##### TCP_NODELAY

 A opção `TCP_NODELAY` controla o algoritmo de Nagle, que gerencia o envio de pequenos pacotes de dados.

1. **Funcionamento padrão (Nagle ativado)**:
   - Combina pequenos pacotes em um único pacote maior
   - Aguarda confirmação (ACK) antes de enviar novo pacote
   - Pode causar atrasos em aplicações sensíveis a latência

2. **Quando desativar (setTcpNoDelay(true))**:
   - Aplicações em tempo real (jogos online, streaming)
   - Sistemas interativos (SSH, VNC, digitação remota)
   - Quando a baixa latência é mais importante que eficiência de rede

3. **Métodos disponíveis**:
   - `setTcpNoDelay(boolean)`: Ativa/desativa o algoritmo
   - `getTcpNoDelay()`: Verifica o estado atual
   - Ambos podem lançar SocketException se não suportado

4. **Impacto no desempenho**:
   - Desativar melhora a responsividade
   - Ativar melhora a eficiência da rede
   - Aumenta o tráfego de rede quando desativado

5. **Recomendações de uso**:
   - Manter ativado para transferências de arquivos grandes
   - Desativar para aplicações interativas
   - Testar em diferentes condições de rede antes de decidir

6. **Exemplo de implementação**:
   ```java
   Socket socket = new Socket();
   socket.setTcpNoDelay(true);  // Desativa buffering
   if(socket.getTcpNoDelay()) {
       System.out.println("Nagle desativado");
   }
   ```

7. **Considerações finais**:
   - Configuração por conexão
   - Não afeta outros sockets
   - Pode ser alterada a qualquer momento
   - Efeito imediato nas operações de rede

##### SO_LINGER

 Controla o comportamento ao fechar um socket com dados pendentes de envio

2. **Comportamento padrão**:
   - `close()` retorna imediatamente
   - Sistema continua tentando enviar dados restantes em segundo plano

3. **Configuração com setSoLinger()**:
   - Primeiro parâmetro (boolean): Ativa/desativa a funcionalidade
   - Segundo parâmetro (int): Tempo máximo de espera em segundos

4. **Efeitos da configuração**:
   - **Linger desativado (on=false)**: Comportamento padrão
   - **Linger ativado com tempo 0**: Descarta pacotes não enviados imediatamente
   - **Linger ativado com tempo >0**: Bloqueia o close() até:
     * Dados serem enviados e confirmados OU
     * Tempo limite ser atingido (dados restantes são descartados)

5. **Valores especiais**:
   - `getSoLinger()` retorna -1 quando a opção está desativada
   - Valor máximo permitido: 65.535 segundos (~18 horas)

6. **Tratamento de erros**:
   - `SocketException` se a opção não for suportada
   - `IllegalArgumentException` para valores negativos

7. **Caso de uso típico**:
   ```java
   if (socket.getSoLinger() == -1) {
       socket.setSoLinger(true, 30); // Espera até 30 segundos
   }
   ```

8. **Recomendações**:
   - Usar com cautela em aplicações que exigem fechamento rápido
   - Evitar tempos muito longos (geralmente poucos segundos são suficientes)
   - Considerar o valor padrão da plataforma na maioria dos casos

9. **Impacto no desempenho**:
   - Pode aumentar o tempo de fechamento da conexão
   - Garante entrega de dados importantes antes de encerrar
   - Melhor para protocolos confiáveis que exigem confirmação

10. **Limitações**:
    - Não afeta dados já em buffer de recebimento
    - Tempo máximo pode variar entre plataformas
    - Não garante entrega completa se o tempo for insuficiente


##### SO_TIMEOUT

1. **Propósito principal**: Controla o tempo máximo de espera em operações de leitura (read()) no socket

2. **Funcionamento padrão**:
   - Chamadas de leitura bloqueiam indefinidamente até receber dados
   - Valor padrão: 0 (timeout infinito)

3. **Configuração**:
   - `setSoTimeout(int milliseconds)`: Define tempo máximo em milissegundos
   - `getSoTimeout()`: Retorna o timeout atual

4. **Comportamento com timeout**:
   - Se a leitura exceder o tempo definido:
     * Lança `InterruptedIOException`
     * Mantém a conexão ativa
     * Permite novas tentativas de leitura

5. **Valores especiais**:
   - 0: Timeout infinito (comportamento padrão)
   - Valores positivos: Tempo máximo em milissegundos

6. **Tratamento de erros**:
   - `SocketException`: Se a opção não for suportada
   - `IllegalArgumentException`: Para valores negativos

7. **Caso de uso típico**:
   ```java
   if (socket.getSoTimeout() == 0) {
       socket.setSoTimeout(30000); // 30 segundos
   }
   ```

8. **Recomendações**:
   - Fundamental para evitar bloqueios permanentes
   - Valores típicos entre 10-120 segundos para maioria das aplicações
   - Sempre tratar `InterruptedIOException`

9. **Impacto na aplicação**:
   - Aumenta robustez contra falhas de rede
   - Permite implementar lógicas de retentativa
   - Mantém controle sobre tempos de resposta

10. **Observações importantes**:
    - Afeta apenas operações de leitura
    - Não encerra a conexão quando ocorre timeout
    - Pode ser ajustado dinamicamente durante a vida do socket



##### SO_RCVBUF and SO_SNDBUF

**1. Conceito Básico**  
- **Buffers TCP**: Áreas de memória que armazenam temporariamente dados durante transferências de rede.  
- **Finalidade**: Melhorar desempenho, equilibrando velocidade e estabilidade.  

**2. Impacto no Desempenho**  
- **Conexões rápidas (>10Mbps)**: Beneficiam-se de buffers maiores (ex: 128KB+).  
- **Conexões lentas (discadas)**: Operam melhor com buffers menores.  
- **Largura de banda máxima**:  
  ```  
  Largura de banda (bytes/s) = Tamanho do buffer (bytes) / Latência (segundos)  
  ```  
  - Exemplo: Buffer de 64KB + latência de 0.5s = ~1Mbps de taxa máxima.  

**3. Métodos em Java**  
- **Para buffers de recebimento (SO_RCVBUF)**:  
  ```java  
  socket.setReceiveBufferSize(int tamanho);  // Define tamanho (em bytes)  
  socket.getReceiveBufferSize();            // Retorna tamanho atual  
  ```  
- **Para buffers de envio (SO_SNDBUF)**:  
  ```java  
  socket.setSendBufferSize(int tamanho);  
  socket.getSendBufferSize();  
  ```  

**4. Comportamento Prático**  
- **Ajuste automático**: Sistemas operacionais podem:  
  - Ignorar valores muito altos (ex: limitar a 64KB em Linux).  
  - Dobrar o tamanho solicitado (ex: pedido de 64KB → 128KB alocado).  
- **Buffer efetivo**: Sempre o menor entre `SO_RCVBUF` e `SO_SNDBUF`.  

**5. Tratamento de Erros**  
- `IllegalArgumentException`: Se o tamanho for ≤ 0.  
- `SocketException`: Rara (geralmente substituída pela verificação de argumento inválido).  

**6. Quando Ajustar?**  
- **Aumentar buffers**: Se a conexão não saturar a banda disponível (ex: 1Mbps em link de 100Mbps).  
- **Diminuir buffers**: Se houver perda de pacotes/congestionamento.  
- **Padrões modernos**: Sistemas operacionais usam *TCP window scaling* para ajuste dinâmico (geralmente dispensando configuração manual).  

**7. Recomendações**  
- **Não otimize prematuramente**: Use valores padrão, a menos que medições indiquem problemas.  
- **Priorize ajustes no SO**: Configurações globais do sistema (ex: `/proc/sys/net/ipv4/tcp_rmem` no Linux) podem ter maior impacto que ajustes por socket.  

**8. Exemplo Prático**  
```java  
try {  
    Socket socket = new Socket("example.com", 80);  
    // Aumenta buffers para 256KB (se suportado)  
    socket.setReceiveBufferSize(256 * 1024);  
    socket.setSendBufferSize(256 * 1024);  
    System.out.println("Buffer recebimento: " + socket.getReceiveBufferSize());  
} catch (SocketException e) {  
    System.err.println("Erro ao configurar buffers: " + e.getMessage());  
}  
```  

**Saída possível** (em sistema com limite de 128KB):  
```  
Buffer recebimento: 131072  // 128KB (ajustado pelo SO)  
```  

**9. Observações Finais**  
- **Eficiência**: Buffers grandes melhoram transferências de arquivos (FTP/HTTP), mas são irrelevantes para sessões interativas (SSH).  
- **Latência vs. Buffer**: Reduzir latência (ex: com redes mais rápidas) tem efeito similar a aumentar o buffer.  
- **Sistemas legados**: Valores padrão antigos (ex: 2KB em BSD 4.2) são inadequados para redes modernas.  

---  
**Nota**: Para medição de latência, use `ping` ou `InetAddress.isReachable()`.


##### SO_KEEPALIVE

###### **1. Funcionamento**  
- **Objetivo**: Detectar se a conexão TCP está inativa devido a falhas no servidor ou rede.  
- **Mecanismo**:  
  - Envia **pacotes de keep-alive** periodicamente (padrão: a cada 2 horas).  
  - Se não houver resposta após **12 minutos**, fecha o socket.  

###### **2. Métodos em Java**  
```java  
socket.setKeepAlive(true);  // Ativa o keep-alive  
socket.getKeepAlive();      // Verifica se está ativo (retorna boolean)  
```  

###### **3. Comportamento Padrão**  
- **Desativado (`false`)**: Conexões podem permanecer abertas indefinidamente, mesmo se o servidor cair.  

###### **4. Quando Usar?**  
- **Conexões de longa duração** (ex: bancos de dados, sessões persistentes).  
- **Para evitar "conexões zumbis"** (quando o servidor cai sem fechar a conexão).  

###### **5. Limitações**  
- **Intervalo fixo**: Tempos entre verificações são determinados pelo SO (normalmente 2 horas).  
- **Não substitui heartbeat customizado**: Para controle mais preciso, implemente seu próprio mecanismo (ex: mensagens periódicas no protocolo da aplicação).  

###### **6. Exemplo Prático**  
```java  
Socket socket = new Socket("servidor.com", 8080);  
socket.setKeepAlive(true);  // Ativa verificação automática  

if (socket.getKeepAlive()) {  
    System.out.println("Keep-alive ativado");  
}  
```  

###### **7. Observações**  
- **Overhead mínimo**: Os pacotes de keep-alive consomem poucos recursos.  
- **Alternativas**: Em aplicações críticas, considere implementar **heartbeats** no nível do protocolo.  

###### **8. Erros**  
- `SocketException`: Se a opção não for suportada (raro em sistemas modernos).  

---  
**Nota**: Ideal para cenários onde é preferível fechar conexões inativas automaticamente, mas não substitui monitoramento ativo em aplicações sensíveis.


##### OOBINLINE

###### **1. Conceito de Dados Urgentes (OOB - Out-of-Band)**  
- **Funcionamento**:  
  - TCP permite enviar **1 byte de dados "urgentes"** fora da sequência normal.  
  - Enviado imediatamente, mesmo com dados regulares em buffer.  
  - O receptor é notificado e pode priorizar o processamento desse byte.  

###### **2. Envio de Dados Urgentes em Java**  

```java  
socket.sendUrgentData(int byte); // Envia o byte menos significativo do argumento  
```  
- **Exemplo**:  
  ```java  
  socket.sendUrgentData('!'); // Envia '!' como dado urgente  
  ```  

###### **3. Recebimento de Dados Urgentes**  

- **Comportamento padrão (`OOBINLINE = false`)**:  
  - Java ignora dados urgentes.  
- **Ativando recebimento inline (`OOBINLINE = true`)**:  
  ```java  
  socket.setOOBInline(true); // Habilita recebimento no fluxo normal  
  ```  
  - Dados urgentes são mesclados ao fluxo de entrada (`InputStream`).  
  - **Sem distinção**: O aplicativo deve identificar manualmente o byte urgente.  

###### **4. Métodos Java**  
```java  
socket.setOOBInline(boolean ativar); // Ativa/desativa  
socket.getOOBInline();              // Verifica status  
```  

###### **5. Casos de Uso**  
- **Sinalização de emergência**: Enviar um comando de cancelamento (ex: `Ctrl+C`).  
- **Priorização em protocolos customizados**: Marcar eventos críticos.  

###### **6. Limitações**  
- **Plataforma-dependente**: Comportamento varia entre sistemas operacionais.  
- **1 byte apenas**: Só é possível enviar um único byte por vez.  
- **Sem tratamento especial**: Java não separa dados urgentes dos regulares quando `OOBINLINE` está ativo.  

###### **7. Exemplo Completo**  
```java  
Socket socket = new Socket("servidor.com", 1234);  
socket.setOOBInline(true); // Ativa recebimento inline  

// Envia um byte urgente  
socket.sendUrgentData('X');  

// Lê dados (o byte 'X' estará no fluxo normal)  
InputStream in = socket.getInputStream();  
int byteUrgente = in.read(); // Pode ser 'X'  
```  

###### **8. Observações**  
- **Raramente usado**: Protocolos modernos geralmente implementam priorização no nível da aplicação.  
- **Alternativas**: Para controle preciso, use **heartbeats** ou mensagens de controle no protocolo.  

###### **9. Erros**  
- `SocketException`: Se a opção não for suportada.  
- `IOException`: Se a conexão estiver fechada.  

---  
**Nota prática**: Útil para cenários específicos onde um byte deve "furar a fila", mas sua implementação exige cuidados devido à variação entre plataformas.


##### SO_REUSEADDR


Permite que um socket reutilize uma porta local imediatamente após o fechamento de outro socket, mesmo se a porta estiver temporariamente em estado de "espera" (TIME_WAIT).  

###### **2. Problema Resolvido**  
- **Sem SO_REUSEADDR**:  
  - Quando um socket fecha uma conexão, a porta fica bloqueada por um tempo (para garantir que pacotes atrasados não sejam entregues a um novo processo).  
  - Isso impede a reutilização imediata em portas bem conhecidas (ex: HTTP na porta 80).  

- **Com SO_REUSEADDR**:  
  - Ignora o estado TIME_WAIT, permitindo que outro socket use a porta imediatamente.  

###### **3. Métodos Java**  
```java  
socket.setReuseAddress(true);  // Ativa a reutilização  
socket.getReuseAddress();      // Verifica o status  
```  

###### **4. Condições para Funcionar**  
1. **Deve ser chamado antes do bind()**:  
   - Use o construtor vazio `Socket()` + `setReuseAddress(true)` antes de `connect()`.  
2. **Ambos os sockets envolvidos** (o que fechou e o novo) devem ter a opção ativada.  

###### **5. Casos de Uso**  
- **Servidores que reiniciam rapidamente**: Evita erros como _"Address already in use"_.  
- **Aplicações que usam portas fixas**: Ex: Servidores web, jogos multiplayer.  

###### **6. Exemplo Prático**  
```java  
// Socket 1 (encerrado anteriormente)  
Socket socket1 = new Socket();  
socket1.setReuseAddress(true);  
socket1.bind(new InetSocketAddress(8080));  
socket1.connect(...);  

// Socket 2 (reutiliza a porta 8080 imediatamente)  
Socket socket2 = new Socket();  
socket2.setReuseAddress(true);  
socket2.bind(new InetSocketAddress(8080)); // Funciona graças ao SO_REUSEADDR  
```  

###### **7. Observações**  
- **Padrão**: Desativado (`false`).  
- **Não afeta segurança**: Pacotes atrasados são descartados pelo kernel.  
- **Uso típico em servidores**: `ServerSocket.setReuseAddress(true)` é mais comum.  

###### **8. Erros**  
- `SocketException`: Se chamado após o bind.  
- `IllegalArgumentException`: Se a porta for inválida.  

---  
**Nota**: Ideal para desenvolvimento e testes, mas use com cautela em produção (entenda as implicações do TIME_WAIT).



##### IP_TOS Class of Service
 

###### **1. Conceito Básico**
- **Objetivo**: Permite definir prioridades para diferentes tipos de tráfego (ex: vídeo, VoIP, e-mail) em redes IP.
- **Armazenamento**: Valor de 8 bits no cabeçalho IP (campo **TOS - Type of Service**).

###### **2. Métodos Java**
```java
socket.setTrafficClass(int valor);  // Define a classe (0-255)
socket.getTrafficClass();          // Retorna o valor atual
```

###### **3. Padrão Moderno (DSCP + ECN)**
- **DSCP (6 bits)**: Define 64 classes de serviço (Expedited Forwarding, Assured Forwarding).
- **ECN (2 bits)**: Notificação explícita de congestionamento (deve ser zero ao definir).

###### **4. Classes Comuns**
| **Classe**               | **Binário** | **Uso**                                  |
|--------------------------|------------|------------------------------------------|
| **Expedited Forwarding** | 101110     | VoIP (baixa latência/perda)              |
| **Assured Forwarding**   | Vários     | Prioridades por classe (1-4) e taxa de descarte |

###### **5. Exemplos Práticos**
- **VoIP (EF)**:
  ```java
  socket.setTrafficClass(0xB8);  // 10111000 (EF + ECN=0)
  ```
- **Prioridades com AF**:
  ```java
  // Classe 1, baixa prioridade
  socket1.setTrafficClass(0x26); // 00100110 (AF11)
  // Classe 4, alta prioridade
  socket2.setTrafficClass(0x8E); // 10001110 (AF43)
  ```

###### **6. Limitações**
- **Suporte variável**: Muitas redes/ISPs ignoram DSCP.
- **Android**: Ignora `setTrafficClass()`.
- **Alternativa**: `setPerformancePreferences()` (mas também com suporte limitado).

###### **7. Recomendações**
- **Use para redes controladas** (ex: corporativas), não para Internet pública.
- **Teste a implementação**: Verifique se o SO/hardware respeita as configurações.

###### **8. Exemplo de Priorização**
```java
// Vídeo (alta largura de banda)
Socket video = new Socket();
video.setPerformancePreferences(1, 2, 3);  // Banda > Latência > Tempo de conexão
```

###### **9. Observações Finais**
- **Não é garantia**: Serve apenas como "dica" para o sistema.
- **Legado**: Ignore a documentação desatualizada sobre "low cost/high reliability".


##### Socket Exceptions

###### **1. Hierarquia de Exceções**
- Todas derivam de `IOException`:
  - `SocketException` (geral)
    - `BindException`
    - `ConnectException`
    - `NoRouteToHostException`
  - `ProtocolException` (direta de `IOException`)

###### **2. Exceções Específicas e Causas**

| **Exceção**               | **Quando Ocorre?**                                                                 | **Solução Sugerida**                          |
|---------------------------|------------------------------------------------------------------------------------|-----------------------------------------------|
| **BindException**         | Tentativa de usar uma porta local ocupada ou sem permissões                        | Escolher outra porta ou verificar permissões  |
| **ConnectException**      | Conexão recusada (host remoto ocupado ou serviço indisponível na porta)            | Verificar se o serviço está ativo no destino  |
| **NoRouteToHostException**| Timeout de conexão (rede congestionada, host inacessível ou firewall bloqueando)   | Testar conectividade básica (ping, telnet)    |
| **ProtocolException**     | Dados recebidos violam as regras TCP/IP                                            | Verificar integridade do protocolo aplicado   |

###### **3. Comportamento Padrão**
- Métodos como `connect()`, `bind()`, e `send()` lançam essas exceções.
- **Não contêm métodos especiais**, mas permitem tratamento granular:
  ```java
  try {
      socket.connect(endereco);
  } catch (ConnectException e) {
      System.err.println("Servidor recusou conexão. Verifique se o serviço está ativo.");
  } catch (NoRouteToHostException e) {
      System.err.println("Falha de rota. Verifique a rede ou firewall.");
  }
  ```

###### **4. Boas Práticas**
- **Tratamento diferenciado**: Use `instanceof` para identificar o tipo de falha.
- **Tentativas de reconexão**: Útil para `NoRouteToHostException` (falhas temporárias).
- **Log detalhado**: Registre a exceção específica para diagnóstico.

###### **5. Exemplo Completo**
```java
try {
    Socket socket = new Socket();
    socket.bind(new InetSocketAddress(8080)); // Pode lançar BindException
    socket.connect(new InetSocketAddress("exemplo.com", 80), 5000);
} catch (BindException e) {
    System.err.println("Porta 8080 já em uso ou bloqueada.");
} catch (ConnectException e) {
    System.err.println("Servidor não aceitou conexão.");
} catch (NoRouteToHostException e) {
    System.err.println("Host inacessível (rede/firewall).");
} catch (SocketException e) {
    System.err.println("Erro genérico de socket: " + e.getMessage());
}
```

###### **6. Observações**
- **Android/IOs**: Algumas exceções podem ter comportamentos ligeiramente diferentes.
- **ProtocolException**: Rara em aplicações comuns; indica corrupção de pacotes TCP/IP.

###### **7. Quando Cada Exceção é Lançada?**
- **BindException**: Durante `new Socket().bind()` ou `ServerSocket()`.
- **ConnectException**: No `connect()`, quando o host ativo recusa explicitamente.
- **NoRouteToHostException**: Quando a rede física impede a conexão (ex: WiFi sem internet).


##### Sockets in GUI Applications

###### **1. Contexto**  
- **Aplicações GUI + Rede**: Java permite criar clientes robustos com interface gráfica (ex: navegadores, IDEs como Eclipse, clientes P2P como Frostwire).  
- **Exemplo Prático**: Cliente **Whois** (protocolo simples para consulta de informações de domínios/administradores na Internet).  

---

###### **2. Protocolo Whois (RFC 954)**  
- **Porta**: 43 (TCP)  
- **Fluxo**:  
  1. Cliente conecta ao servidor Whois.  
  2. Envia uma string de busca (ex: `"example.com\r\n"`).  
  3. Servidor retorna dados em texto (informações de contato, registro de domínio, etc.).  
  4. Conexão é fechada pelo servidor.  

##### Whois

###### **1. Definição e Objetivo**
- **Whois**: Protocolo de serviço de diretório (RFC 954) para consultar informações sobre domínios e administradores na Internet.
- **Funcionamento Básico**:
  1. Conexão via TCP na **porta 43**.
  2. Envio de uma string de busca (ex: `"google.com\r\n"`).
  3. Resposta em texto livre com detalhes do domínio/pessoa.
  4. Conexão é fechada pelo servidor.

###### **2. Exemplo de Consulta Simples**
```bash
telnet whois.internic.net 43
> google.com
```
**Saída Típica**:
```
Domain Name: GOOGLE.COM
Registrar: MarkMonitor Inc.
Registrant Email: abuse@markmonitor.com
Creation Date: 1997-09-15
```

###### **3. Problemas do Whois Tradicional**
- **Centralizado**: Depende de servidores específicos (ex: `whois.internic.net`).
- **Formato Não Padronizado**: Cada servidor retorna dados em layouts diferentes (ex: Internic vs. whois.nic.fr).
- **Alternativa**: Whois++ (RFCs 1913/1914), mas pouco adotado.

###### **4. Implementação em Java**
**Cliente Básico**:
```java
public class WhoisClient {
    public static String query(String host, String query) throws IOException {
        try (Socket socket = new Socket(host, 43);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {
            
            out.write((query + "\r\n").getBytes());
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static void main(String[] args) throws IOException {
        String resposta = query("whois.verisign-grs.com", "google.com");
        System.out.println(resposta);
    }
}
```

###### **5. Desafios no Processamento de Respostas**
- **Parsing Complexo**: Diferentes servidores usam formatos distintos (ex: `nic-hdl` na França vs. domínios no Internic).
- **Exemplo de Tratamento**:
  ```java
  if (resposta.contains("Domain Name:")) {
      // Processa formato de domínio (ex: Internic)
  } else if (resposta.contains("nic-hdl:")) {
      // Processa formato francês (whois.nic.fr)
  }
  ```

###### **6. Prefixos de Busca Avançada**
| **Prefixo**   | **Filtro**               | **Exemplo**              |
|---------------|--------------------------|--------------------------|
| `domain`      | Apenas domínios          | `domain google.com`      |
| `person`      | Apenas pessoas           | `person Harold`          |
| `!`           | Busca por handle         | `!H12345-FRNIC`          |
| `partial`     | Busca parcial (início)   | `partial Elliot`         |

###### **7. Integração com GUI (Swing)**
**Boas Práticas**:
- Use `SwingWorker` para evitar bloqueio da interface durante consultas.
- Exemplo:
  ```java
  new SwingWorker<String, Void>() {
      @Override
      protected String doInBackground() throws Exception {
          return WhoisClient.query(servidor, consulta);
      }
      @Override
      protected void done() {
          // Atualiza a GUI com o resultado
      }
  }.execute();
  ```

###### **8. Limitações e Alternativas Modernas**
- **Problemas**: Centralização, falta de padronização.
- **Alternativas**: APIs REST (ex: RDAP) ou bibliotecas como Apache Commons Net.

###### **9. Casos de Uso Reais**
- Verificar disponibilidade de domínios.
- Identificar contatos administrativos para reportar abusos.
- Integrar a ferramentas de monitoramento de infraestrutura.

###### **10. Conclusão**
- **Whois** é útil para consultas simples, mas exige tratamento manual devido à falta de padronização.
- Em Java, a implementação é direta com `Socket`, mas o parsing da resposta pode ser complexo.
- Para aplicações GUI, sempre use threads separadas para operações de rede.


##### A Network Client Library

###### **1. Estrutura da Biblioteca**
- **Classe `Whois`**: Encapsula toda a lógica de conexão e consulta ao protocolo Whois.
- **Campos principais**:
  - `host`: Endereço do servidor Whois (padrão: `whois.internic.net`).
  - `port`: Porta do serviço (padrão: `43`).
- **Construtores**: Permitem configurar servidor e porta de diferentes formas.

###### **2. Funcionalidade Central**
- **Método `lookUpNames()`**:
  - **Parâmetros**:
    - `target`: Termo de busca (ex: `"google.com"`).
    - `category`: Tipo de registro (enum `SearchFor`: `DOMAIN`, `PERSON`, etc.).
    - `group`: Campo de busca (enum `SearchIn`: `NAME`, `MAILBOX`, etc.).
    - `exactMatch`: Busca exata ou parcial.
  - **Processo**:
    1. Conecta ao servidor via `Socket`.
    2. Envia a consulta formatada (ex: `"Domain google.com"`).
    3. Lê a resposta e retorna como `String`.

###### **3. Enums para Tipos de Busca**
- **`SearchFor`**: Categorias de registros (ex: `NETWORK`, `HOST`).
- **`SearchIn`**: Campos específicos (ex: `NAME`, `HANDLE`).
- **Vantagem**: Segurança de tipos em tempo de compilação.

###### **4. Interface Gráfica (`WhoisGUI`)**
- **Componentes**:
  - Campo de texto para consulta (`searchString`).
  - Área de resultados (`names`).
  - Botões de opção para filtros (`searchIn`, `searchFor`).
  - Checkbox para busca exata (`exactMatch`).
- **Fluxo**:
  1. Usuário insere consulta e clica em "Find".
  2. `SwingWorker` executa a consulta em segundo plano.
  3. Atualiza a interface com os resultados.

###### **5. Tratamento Assíncrono**
- **Problema**: Operações de rede bloqueiam a thread de interface (EDT).
- **Solução**: Uso de `SwingWorker`:
  - **`doInBackground()`**: Executa a consulta Whois.
  - **`done()`**: Atualiza a GUI na EDT com os resultados.

###### **6. Exemplo de Consulta**
```java
Whois whois = new Whois("whois.verisign-grs.com");
String response = whois.lookUpNames("google.com", 
                                   Whois.SearchFor.DOMAIN, 
                                   Whois.SearchIn.ALL, 
                                   true);
```

###### **7. Melhorias Possíveis**
- **Persistência**: Salvar resultados em arquivo.
- **Tratamento de Erros**: Melhor feedback para falhas de conexão.
- **Cache**: Armazenar consultas recentes localmente.

###### **8. Pontos-Chave**
- **Separação de Responsabilidades**: Lógica de rede isolada da GUI.
- **Thread-Safety**: `SwingWorker` garante atualizações seguras da interface.
- **Extensibilidade**: Fácil adição de novos servidores Whois ou filtros.

###### **9. Código de Exemplo (Consulta Simples)**
```java
public static void main(String[] args) {
    try {
        Whois whois = new Whois();
        String result = whois.lookUpNames("example.com", 
                                         Whois.SearchFor.DOMAIN, 
                                         Whois.SearchIn.ALL, 
                                         true);
        System.out.println(result);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

###### **10. Conclusão**
- **Reusabilidade**: A classe `Whois` pode ser integrada em qualquer aplicação Java.
- **Flexibilidade**: Suporta múltiplos servidores e tipos de consulta.
- **Boas Práticas**: Modelo de design que separa claramente lógica de rede e interface.

