
## Sockets for Servers

A classe `Socket` não é adequada para criar servidores, já que um servidor não sabe antecipadamente qual cliente tentará se conectar ou quando isso acontecerá.  
Para lidar com essa situação, Java fornece a classe `ServerSocket`, que representa sockets do lado do servidor. Sua função é aguardar conexões de entrada, assim como um recepcionista espera chamadas telefônicas. Tecnicamente, um `ServerSocket` fica em um *port* específico do servidor, aguardando tentativas de conexão de clientes remotos. Quando um cliente tenta se conectar, o servidor estabelece a comunicação e retorna um objeto `Socket` convencional para troca de dados entre as duas máquinas.

Em resumo:
- **ServerSocket**: Espera passivamente por conexões (lado servidor).
- **Socket**: Inicia conexões (lado cliente) ou troca dados após a conexão ser estabelecida.

Assim, enquanto o cliente inicia a comunicação, o servidor responde às solicitações usando um `Socket` comum após a conexão ser negociada.


##### Using ServerSockets

A classe `ServerSocket` em Java fornece os métodos necessários para criar servidores. O ciclo básico de um servidor é:

1. **Criação do `ServerSocket`**:
    - Um novo `ServerSocket` é criado em uma porta específica (ex.: porta 13 para o protocolo *daytime*).

2. **Aguardando conexões (`accept()`)**
    - O método `accept()` bloqueia até que um cliente se conecte, retornando um objeto `Socket` para comunicação.

3. **Troca de dados com o cliente**
    - Obtém-se `InputStream` ou `OutputStream` do `Socket` para enviar/receber dados.
    - No caso do *daytime*, o servidor envia a data/hora atual em formato legível.

4. **Fechamento da conexão**
    - Após a resposta, o servidor fecha o `Socket`.

5. **Loop infinito**
    - O servidor retorna ao passo 2 para aceitar novas conexões.

###### **Exemplo: Servidor Daytime**

```java
ServerSocket server = new ServerSocket(13);  
while (true) {  
    try (Socket connection = server.accept()) {  
        Writer out = new OutputStreamWriter(connection.getOutputStream());  
        out.write(new Date().toString() + "\r\n");  
        out.flush();  
    } catch (IOException ex) { /* Ignora falhas em conexões individuais */ }  
}  
```  

###### **Pontos importantes**
- **Bloco `try-with-resources`**: Garante que os recursos (`Socket`, `OutputStream`) sejam fechados automaticamente.
- **Tratamento de exceções**:
    - Erros em conexões específicas não devem derrubar o servidor.
    - Falhas críticas (ex.: falha ao criar o `ServerSocket`) encerram o programa.
- **Protocolo *Daytime***:
    - Envia apenas uma resposta antes de fechar a conexão.
    - Usa `\r\n` para quebra de linha (padrão em redes).

###### **Observações**
- **Servidores iterativos**: Processam uma conexão por vez (simples, mas pode causar atrasos).
- **Portas privilegiadas (ex.: 13)**: No Unix, exigem permissão de *root*; use portas >1024 para testes.

**Exemplo de saída (via Telnet):**
```
$ telnet localhost 13  
Wed Aug 07 14:30:00 BRT 2024  
Connection closed by foreign host.  
```  

Este modelo básico pode ser expandido para protocolos mais complexos (ex.: HTTP, FTP) com múltiplas requisições por conexão ou uso de *threads* para concorrência.


##### Serving Binary Data

O envio de dados binários (não-texto) por um servidor segue um princípio semelhante ao de servidores de texto, mas utiliza `OutputStream` para escrever bytes em vez de `Writer` para strings.

###### **Exemplo: Servidor de Tempo Binário (RFC 868)**

O protocolo de tempo (porta **37**) envia um inteiro de **4 bytes** (big-endian, sem sinal) representando os segundos desde **1º de janeiro de 1900, 00:00 GMT**.

**Passos do servidor:**
1. **Ajuste de época**:
    - Java usa **1970** como época (`Date`), enquanto o protocolo usa **1900**.
    - Conversão necessária:
      ```java
      long diferencaEntreEpocas = 2208988800L; // Segundos entre 1900 e 1970
      ```  

2. **Cálculo do tempo atual em segundos**:
   ```java
   Date agora = new Date();
   long msDesde1970 = agora.getTime();
   long segundosDesde1970 = msDesde1970 / 1000;
   long segundosDesde1900 = segundosDesde1970 + diferencaEntreEpocas;
   ```  

3. **Conversão para 4 bytes (big-endian)**:
   ```java
   byte[] tempo = new byte[4];
   tempo[0] = (byte) ((segundosDesde1900 & 0xFF000000L) >> 24); // Byte mais significativo
   tempo[1] = (byte) ((segundosDesde1900 & 0x00FF0000L) >> 16);
   tempo[2] = (byte) ((segundosDesde1900 & 0x0000FF00L) >> 8);
   tempo[3] = (byte) (segundosDesde1900 & 0x000000FFL);        // Byte menos significativo
   ```  

4. **Envio e fechamento**:
   ```java
   OutputStream out = connection.getOutputStream();
   out.write(tempo); // Envia os 4 bytes
   out.flush();
   ```  

###### **Código Completo**

```java
import java.io.*;
import java.net.*;
import java.util.Date;

public class ServidorTempo {
    public static final int PORTA = 37;

    public static void main(String[] args) {
        long diferencaEntreEpocas = 2208988800L;

        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            while (true) {
                try (Socket conexao = servidor.accept()) {
                    OutputStream out = conexao.getOutputStream();
                    long segundosDesde1900 = (System.currentTimeMillis() / 1000) + diferencaEntreEpocas;
                    byte[] tempo = new byte[4];
                    tempo[0] = (byte) ((segundosDesde1900 >> 24) & 0xFF);
                    tempo[1] = (byte) ((segundosDesde1900 >> 16) & 0xFF);
                    tempo[2] = (byte) ((segundosDesde1900 >> 8) & 0xFF);
                    tempo[3] = (byte) (segundosDesde1900 & 0xFF);
                    out.write(tempo);
                    out.flush();
                } catch (IOException ex) {
                    System.err.println("Erro na conexão: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            System.err.println("Erro no servidor: " + ex);
        }
    }
}
```  

###### **Pontos-Chave**
- **Dados binários**: Usa-se `OutputStream` para bytes, não `Writer` (texto).
- **Formato do protocolo**:
    - 4 bytes, big-endian, sem sinal.
    - Epoch diferente (1900 vs. 1970) requer ajuste.
- **Operações bit-a-bit**: Isolam cada byte do inteiro (`>>`, `& 0xFF`).
- **Porta 37**: Exige privilégios de *root* no Unix; para testes, use portas >1024.

**Diferença do servidor de texto (Daytime)**:
- Envia **bytes brutos** em vez de strings.
- Útil para protocolos que exigem eficiência (ex.: streaming de áudio/vídeo).

Este modelo é base para protocolos binários como FTP (transmissão de arquivos) ou DNS.



##### Multithreaded Servers

Servidores iterativos (que processam uma conexão por vez) são adequados para protocolos rápidos, como *daytime* e *time*, mas podem ter problemas com clientes lentos ou travados, causando atrasos para outros clientes. Para resolver isso, usamos **servidores multithread**, onde cada conexão é tratada em uma thread separada, permitindo atendimento simultâneo.

###### **Problemas dos Servidores Iterativos**
- **Bloqueio**: Um cliente lento/travado paralisa o servidor para outros.
- **Fila de conexões**: O SO armazena conexões não atendidas em uma fila (tamanho padrão = 50). Se a fila encher, novas conexões são recusadas.

###### **Solução: Thread por Conexão**
Cada conexão é delegada a uma nova thread, liberando a thread principal para aceitar mais clientes.

**Exemplo 9-3: Servidor Daytime Multithread**
```java
public class MultithreadedDaytimeServer {
    public static final int PORT = 13;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket connection = server.accept();
                Thread task = new DaytimeThread(connection); // Cria uma thread para a conexão
                task.start();
            }
        } catch (IOException ex) { /* Trata erro no servidor */ }
    }

    private static class DaytimeThread extends Thread {
        private Socket connection;

        @Override
        public void run() {
            try (Writer out = new OutputStreamWriter(connection.getOutputStream())) {
                out.write(new Date().toString() + "\r\n");
            } catch (IOException ex) { /* Trata erro na thread */ }
            finally {
                try { connection.close(); } catch (IOException e) { /* Ignora */ }
            }
        }
    }
}
```  

**Problema**:
- **Ataque de negação de serviço (DoS)**: Muitas conexões simultâneas podem criar threads infinitas, esgotando a memória da JVM.

###### **Solução Melhor: Pool de Threads**
Usa um número fixo de threads (ex.: 50) para limitar o uso de recursos. Conexões excedentes aguardam até que uma thread esteja disponível.

**Exemplo 9-4: Servidor com Thread Pool**
```java
public class PooledDaytimeServer {
    public static final int PORT = 13;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(50); // Pool com 50 threads

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket connection = server.accept();
                Callable<Void> task = new DaytimeTask(connection); // Tarefa para o pool
                pool.submit(task);
            }
        } catch (IOException ex) { /* Trata erro no servidor */ }
    }

    private static class DaytimeTask implements Callable<Void> {
        private Socket connection;

        @Override
        public Void call() {
            try (Writer out = new OutputStreamWriter(connection.getOutputStream())) {
                out.write(new Date().toString() + "\r\n");
            } catch (IOException ex) { /* Trata erro na tarefa */ }
            finally {
                try { connection.close(); } catch (IOException e) { /* Ignora */ }
            }
            return null;
        }
    }
}
```  

###### **Diferenças Chave**
| **Aspecto**               | **Thread por Conexão**                          | **Thread Pool**                          |  
|---------------------------|------------------------------------------------|------------------------------------------|  
| **Escalabilidade**         | Risco de esgotar recursos (threads ilimitadas) | Limita threads (ex.: 50) para evitar DoS |  
| **Gerenciamento**          | Mais simples (mas perigoso)                    | Mais robusto (usa `ExecutorService`)     |  
| **Overhead**               | Alto (criação/destruição de threads)           | Baixo (reutilização de threads)          |  

###### **Quando Usar**
- **Thread por Conexão**: Protocolos rápidos e com baixa concorrência.
- **Thread Pool**: Serviços com demanda imprevisível ou processamento demorado (ex.: FTP, HTTP).

**Nota**: Sempre feche as conexões (`connection.close()`) para evitar vazamentos de recursos, mesmo em cenários de erro.


##### Writing to Servers with Sockets

Nos exemplos anteriores, o servidor apenas escrevia em sockets de clientes, sem ler deles. No entanto, a maioria dos protocolos exige que o servidor **leia e escreva** nos sockets. Para isso, é necessário obter tanto um `InputStream` (para ler dados do cliente) quanto um `OutputStream` (para enviar dados de volta). O principal desafio é entender o **protocolo**: quando ler e quando escrever.

###### **Protocolo Echo (RFC 862)**
Um dos protocolos mais simples é o **echo**, que opera na porta **7**. Nele:
- O cliente envia dados ao servidor.
- O servidor **repete (ecoa)** os mesmos dados de volta.
- O cliente fecha a conexão quando desejar.

**Exemplo com Telnet:**
```bash
$ telnet rama.poly.edu 7  
Trying 128.238.10.212...  
Connected to rama.poly.edu.  
This is a test  
This is a test  
^]  
telnet> close  
```  
O protocolo ecoa **byte a byte**, sem depender de linhas ou codificação específica.

###### **Diferenças em Relação a Outros Protocolos**
- O **cliente** é responsável por fechar a conexão (diferente de protocolos como *daytime*).
- Não há uma ordem rígida de requisição-resposta, permitindo comunicação assíncrona.
- O servidor deve suportar **múltiplas threads** para lidar com vários clientes simultaneamente.

###### **Implementação do Servidor Echo (Java NIO)**
O código do servidor usa `ServerSocketChannel` e `Selector` para lidar com conexões de forma não bloqueante:
1. **Aceita conexões** e registra canais para leitura/escrita.
2. **Lê dados** do cliente (`isReadable()`) e os armazena em um buffer.
3. **Escreve os dados** de volta (`isWritable()`) quando há conteúdo no buffer.

**Exemplo simplificado:**
```java
ServerSocketChannel serverChannel = ServerSocketChannel.open();  
serverChannel.bind(new InetSocketAddress(port));  
Selector selector = Selector.open();  
serverChannel.register(selector, SelectionKey.OP_ACCEPT);  

while (true) {  
    selector.select();  
    // Processa conexões, leitura e escrita...  
}  
```  
O servidor suporta até **500 threads** para conexões simultâneas.

O protocolo **echo** é útil para testes de rede, garantindo que os dados não sejam corrompidos. Sua implementação requer tratamento assíncrono de leitura/escrita, com o cliente controlando o fechamento da conexão.

##### Closing Server Sockets

###### **Por que fechar um ServerSocket?**
- Libera a **porta** para uso por outros programas.
- Encerra todas as **conexões ativas** aceitas por esse socket.
- Se não for fechado explicitamente, o sistema operacional o libera quando o programa termina, mas é uma boa prática fazer isso manualmente.

---  

###### **Como Fechar um ServerSocket**

1. **Padrão `try-finally` (Java tradicional)**
   ```java  
   ServerSocket server = null;  
   try {  
       server = new ServerSocket(port);  
       // ... trabalha com o socket  
   } finally {  
       if (server != null) {  
           try {  
               server.close();  
           } catch (IOException ex) {  
               // Ignora o erro (ou registra em log)  
           }  
       }  
   }  
   ```  

2. **Usando `bind()` separadamente (mais flexível)**
   ```java  
   ServerSocket server = new ServerSocket();  
   try {  
       SocketAddress address = new InetSocketAddress(port);  
       server.bind(address);  
       // ... trabalha com o socket  
   } finally {  
       try {  
           server.close();  
       } catch (IOException ex) {  
           // Ignora  
       }  
   }  
   ```  

3. **Java 7+ (`try-with-resources` – forma mais limpa)**
   ```java  
   try (ServerSocket server = new ServerSocket(port)) {  
       // ... trabalha com o socket  
   } // Fecha automaticamente  
   ```  

---  

###### **Verificando o Estado do ServerSocket**
- **`isClosed()`** → Retorna `true` se o socket foi fechado.
- **`isBound()`** → Retorna `true` se o socket já foi vinculado a uma porta (mesmo que fechado depois).

**Método para verificar se um ServerSocket está aberto:**
```java  
public static boolean isOpen(ServerSocket ss) {  
    return ss.isBound() && !ss.isClosed();  
}  
```  

---  

###### **Observações Importantes**
- Um **ServerSocket fechado não pode ser reaberto**, mesmo na mesma porta.
- Se um `ServerSocket` foi criado com o construtor vazio (`new ServerSocket()`) e nunca vinculado a uma porta, `isClosed()` retorna `false`.

**Boas Práticas:**  
✔ Sempre feche o `ServerSocket` em um bloco `finally` ou use `try-with-resources`.  
✔ Verifique o estado do socket antes de reutilizá-lo.  
✔ Ignore ou registre exceções de fechamento, mas não as deixe interromper o fluxo do programa.


##### Logging

###### **Por que Registrar Logs?**
- Servidores rodam **sem supervisão** por longos períodos, tornando os logs essenciais para **depuração** e **auditoria**.
- Logs ajudam a rastrear **requisições** e **erros** mesmo muito tempo depois que ocorreram.

###### **O que Registrar?**
1. **Logs de Requisições (Auditoria)**
    - Registram cada **conexão** ou **operação** (ex.: uma entrada por chamada de API ou ação do cliente).
    - Exemplo: Um servidor de dicionário registra cada palavra buscada.
    - Inclui **erros do cliente** (ex.: requisições malformadas, desconexões inesperadas).

2. **Logs de Erros**
    - Capturam **exceções inesperadas** (ex.: `NullPointerException`, falhas no servidor).
    - **Não inclui** erros do cliente (esses vão nos logs de requisições).
    - **Objetivo ideal:** **Zero entradas** – todo erro registrado deve ser investigado e corrigido.
    - Se uma exceção for considerada "normal", **remova sua entrada** para evitar ruído.

###### **Boas Práticas**
✅ **Evite Logs de Depuração em Produção**
- **Não** registre cada entrada de método ou evento trivial – isso polui os logs e esconde problemas reais.
- Logs de depuração devem ficar em um **arquivo separado** e ser **desativados** em produção.

✅ **Análise e Filtragem de Logs**
- Sistemas avançados permitem filtrar logs (ex.: mostrar apenas níveis `ERROR` ou `INFO`).
- Facilita o gerenciamento, mas **não justifica logs excessivos**.

❌ **Evite o Antipadrão "Registrar Tudo"**
- Logs "por precaução" tornam-se **inúteis e difíceis de analisar**.
- **Necessidades reais de depuração raramente são previsíveis** – foque no que é relevante.

###### **Princípio Fundamental**
- **Todo log deve ter um propósito.**
- **Logs inúteis ocupam espaço e atrapalham a identificação de problemas reais.**
- **Logs de erro devem conter apenas problemas acionáveis.**

###### **Exemplo Prático**
```java  
try {  
    // Processa requisição  
} catch (UnexpectedServerException e) {  
    log.error("Falha crítica no processamento", e); // Registra apenas erros relevantes  
}  
```  

**Logs existem para resolver problemas – não para armazenar dados "por via das dúvidas".**


##### How to Log

###### **Bibliotecas de Logging**
- **Java 1.4+** possui `java.util.logging` (JUL), suficiente para a maioria dos casos.
- Evita dependências de bibliotecas externas como Log4j ou Apache Commons Logging.

###### **Criando um Logger**

- Normalmente, **um logger por classe**, armazenado em um campo `static final`:
  ```java  
  private final static Logger auditLogger = Logger.getLogger("requests");  
  ```  
- **Thread-safe**, podendo ser compartilhado entre várias threads.

###### **Níveis de Log**

| Nível (gravidade decrescente) | Uso Recomendado |  
|-------------------------------|-----------------|  
| `SEVERE`                      | Erros críticos  |  
| `WARNING`                     | Alertas         |  
| `INFO`                        | Logs de auditoria (ex.: requisições) |  
| `CONFIG`, `FINE`, `FINER`, `FINEST` | **Apenas para desenvolvimento** |  

###### **Métodos de Registro**

- Básico:
  ```java  
  logger.log(Level.SEVERE, "Erro inesperado: " + ex.getMessage(), ex);  
  ```  
- Métodos auxiliares para `INFO`, `WARNING`, `SEVERE`:
  ```java  
  logger.info(new Date() + " Conexão de " + connection.getRemoteSocketAddress());  
  ```  

---  

###### **Exemplo Prático: Servidor Daytime com Logs**

```java  
public class LoggingDaytimeServer {  
    private final static Logger auditLogger = Logger.getLogger("requests");  
    private final static Logger errorLogger = Logger.getLogger("errors");  

    public static void main(String[] args) {  
        try (ServerSocket server = new ServerSocket(PORT)) {  
            while (true) {  
                try {  
                    Socket connection = server.accept();  
                    auditLogger.info(new Date() + " " + connection.getRemoteSocketAddress());  
                    // ... processa requisição  
                } catch (IOException ex) {  
                    errorLogger.log(Level.SEVERE, "Erro de conexão", ex);  
                }  
            }  
        } catch (IOException ex) {  
            errorLogger.log(Level.SEVERE, "Falha ao iniciar servidor", ex);  
        }  
    }  
}  
```  

###### **Boas Práticas no Exemplo**

- **Logs de auditoria (`INFO`)**: Registram cada conexão (data + endereço do cliente).
- **Logs de erro (`SEVERE`)**: Capturam exceções inesperadas (ex.: falha ao iniciar servidor).
- **Ignora erros não críticos**: Ex.: cliente desconectado durante uma resposta.

---  

###### **Configuração de Logs em Arquivo**

1. **Arquivo de propriedades (`logging.properties`)**
```properties  
   handlers=java.util.logging.FileHandler  
   java.util.logging.FileHandler.pattern = /var/logs/daytime/requests.log  
   java.util.logging.FileHandler.limit = 10000000  # 10 MB por arquivo  
   java.util.logging.FileHandler.count = 2          # Rotaciona (2 arquivos)  
   java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter  
   requests.level = INFO  
   errors.level = SEVERE  
```  

2. **Passar configuração via JVM**:
```bash  
   java -Djava.util.logging.config.file=/caminho/logging.properties LoggingDaytimeServer  
```  

---  

###### **Observações Importantes**
- **Separe logs por finalidade**: Auditoria vs. erros (requer configuração adicional ou subclasses de `FileHandler`).
- **Monitore os logs regularmente**: Logs não verificados são inúteis.
- **Implemente rotação e retenção**: Evite que arquivos cresçam indefinidamente.

###### **Regra de Ouro**
> **"Se ninguém vai olhar, não registre."**
- Logs excessivos poluem e dificultam a identificação de problemas reais.
- Foque em **informações acionáveis** (ex.: erros críticos, eventos de auditoria essenciais).

---  

**Exemplo de Saída de Log:**
```  
SEVERE: Falha ao iniciar servidor [Sat Apr 13 10:07:01 EDT 2013]  
INFO: Sat Apr 13 10:08:05 EDT 2013 /0:0:0:0:0:0:0:1:57275 [Sat Apr 13 10:08:05 EDT 2013]  
```



##### Constructing Server Sockets

Existem quatro construtores públicos para `ServerSocket`:

1. `public ServerSocket(int port) throws BindException, IOException`
2. `public ServerSocket(int port, int queueLength) throws BindException, IOException`
3. `public ServerSocket(int port, int queueLength, InetAddress bindAddress) throws IOException`
4. `public ServerSocket() throws IOException`

Esses construtores permitem especificar:
- A **porta** que o servidor irá escutar.
- O **tamanho da fila** para requisições de conexão pendentes.
- O **endereço de rede local** ao qual o socket será vinculado.

###### Exemplos de Uso:

1. **Servidor HTTP na porta 80**:
   ```java  
   ServerSocket httpd = new ServerSocket(80);  
   ```  

2. **Servidor com fila de até 50 conexões pendentes**:
   ```java  
   ServerSocket httpd = new ServerSocket(80, 50);  
   ```  
   (Se o tamanho exceder o limite do sistema, o valor máximo do sistema será usado.)

3. **Vinculação a um endereço IP específico**:  
   Útil quando o servidor tem múltiplas interfaces de rede. Exemplo:
   ```java  
   InetAddress local = InetAddress.getByName("192.168.210.122");  
   ServerSocket httpd = new ServerSocket(5776, 10, local);  
   ```  
   (O socket só escutará conexões no IP `192.168.210.122`, ignorando outros endereços da máquina.)

###### Portas Anônimas (Sistema Escolhe)

Passar `0` como porta faz o sistema selecionar uma porta disponível automaticamente. Útil para protocolos como FTP, onde uma porta secundária é usada para transferência de dados.

###### Tratamento de Erros

- Se a porta já estiver em uso ou se não houver permissão (em Unix, portas 1-1023 exigem `root`), uma `IOException` (geralmente `BindException`) será lançada.

###### Exemplo: Verificador de Portas Locais (`LocalPortScanner`)

O programa abaixo tenta criar um `ServerSocket` em cada porta (1-65535) para identificar quais já estão em uso:
```java  
import java.io.*;  
import java.net.*;  

public class LocalPortScanner {  
    public static void main(String[] args) {  
        for (int port = 1; port <= 65535; port++) {  
            try {  
                ServerSocket server = new ServerSocket(port);  
            } catch (IOException ex) {  
                System.out.println("Há um servidor na porta " + port + ".");  
            }  
        }  
    }  
}  
```  
**Saída típica (Windows):**
```  
Há um servidor na porta 135.  
Há um servidor na porta 1025.  
...  
```  
(Em Unix/Linux sem privilégios de `root`, só verifica portas acima de 1023.)

---  
Este resumo destaca os principais pontos sobre a criação e configuração de `ServerSocket` em Java, incluindo tratamento de erros e um exemplo prático de varredura de portas.


##### Constructing Without Binding

O construtor sem argumentos de `ServerSocket` cria um objeto, mas **não o vincula a uma porta** inicialmente, impedindo-o de aceitar conexões até que seja explicitamente vinculado.

###### **Métodos de Vinculação (`bind()`)**

Para vincular o `ServerSocket` posteriormente, usam-se os métodos:
1. `public void bind(SocketAddress endpoint) throws IOException`
2. `public void bind(SocketAddress endpoint, int queueLength) throws IOException`

###### **Utilidade Principal**

Permite configurar **opções do socket** antes da vinculação, já que algumas propriedades não podem ser alteradas após o bind.

###### **Padrão de Uso**
```java  
ServerSocket ss = new ServerSocket();  // Cria sem vincular  
// Configura opções do socket antes do bind  
SocketAddress http = new InetSocketAddress(80);  // Define porta 80  
ss.bind(http);  // Vincula o socket à porta  
```  

###### **Vinculação a uma Porta Aleatória**

Passar `null` como `SocketAddress` faz o sistema selecionar uma porta disponível automaticamente (equivalente a usar `0` nos outros construtores):
```java  
ss.bind(null);  // Escolhe uma porta efêmera  
```  

---  
###### **Pontos-Chave:**
- Útil para definir configurações avançadas (como timeout, buffer size, etc.) antes de fixar a porta.
- Se não for feito `bind()`, o `ServerSocket` não funcionará.
- Permite flexibilidade em cenários como servidores FTP, onde portas dinâmicas são necessárias.


##### Getting Information About a Server Socket

A classe `ServerSocket` fornece métodos para recuperar informações sobre o endereço e a porta em que o socket está vinculado. Isso é útil principalmente quando:
- O socket foi criado em uma **porta anônima** (usando `0` no construtor).
- O socket está vinculado a uma **interface de rede não especificada**.

---  

###### **1. `getInetAddress()`**
Retorna o **endereço IP local** ao qual o `ServerSocket` está vinculado:
```java  
public InetAddress getInetAddress()  
```  
- Se o servidor tiver **apenas um IP**, retorna o mesmo que `InetAddress.getLocalHost()`.
- Se o servidor tiver **vários IPs**, retorna um deles (não há garantia de qual).
- Retorna `null` se o socket **ainda não foi vinculado** a uma interface.

**Exemplo:**
```java  
ServerSocket httpd = new ServerSocket(80);  
InetAddress ia = httpd.getInetAddress();  
```  

---  

###### **2. `getLocalPort()`**
Retorna a **porta local** em que o `ServerSocket` está escutando:
```java  
public int getLocalPort()  
```  
- Útil quando o socket foi criado com **porta `0`** (aleatória).
- Retorna `-1` se o socket **ainda não foi vinculado**.

**Exemplo (socket em porta aleatória):**
```java  
ServerSocket server = new ServerSocket(0);  
System.out.println("Porta usada: " + server.getLocalPort());  
```  

**Saída (execuções diferentes retornam portas diferentes):**
```  
Porta usada: 1154  
Porta usada: 1155  
Porta usada: 1156  
```  

---  

###### **Método `toString()`**
Ao imprimir um `ServerSocket`, o formato padrão é:
```  
ServerSocket[addr=0.0.0.0,port=0,localport=5776]  
```  
- **`addr`**: Endereço IP vinculado (`0.0.0.0` significa "todas as interfaces").
- **`port`**: Sempre `0` (não confundir com `localport`).
- **`localport`**: A porta real em que o socket está escutando.

**Observação:**
- Útil para **debugging**, mas não deve ser usado em lógica de programa.

---  

###### **Casos de Uso Comuns**
1. **Servidores FTP**:
    - O socket principal (porta 21) informa aos clientes qual porta aleatória será usada para transferência de dados.

2. **Aplicações P2P**:
    - Um servidor principal pode direcionar clientes para sub-servidores em portas dinâmicas.

3. **Testes e Prototipagem**:
    - Permite alocar portas temporárias sem conflitos.

---  

###### **Resumo Final**
- **`getInetAddress()`** → Retorna o IP do servidor (`null` se não vinculado).
- **`getLocalPort()`** → Retorna a porta do servidor (`-1` se não vinculado).
- **Porta `0`** → Usada para alocação automática de portas livres.
- **`toString()`** → Formato padrão para depuração, mas não para lógica de aplicação.

Esses métodos são essenciais para gerenciar sockets dinâmicos e obter informações de configuração em tempo de execução.


####cap
#####session
######sub-session

##### Socket Options

As opções de soquete especificam como os soquetes nativos dos quais a classe ServerSocket depende enviam e recebem dados.
Para soquetes de servidor, Java suporta três opções:

+ SO_TIMEOUT
+ SO_REUSEADDR
+ SO_RCVBUF

Ele também permite que você defina preferências de desempenho para os pacotes do soquete.

##### SO_TIMEOUT

O **`SO_TIMEOUT`** define o tempo máximo (em milissegundos) que o método `accept()` aguarda por uma conexão antes de lançar uma **`java.io.InterruptedIOException`**.

###### **Principais Características**
- **Valor padrão:** `0` (nunca expira).
- **Uso típico:** Segurança ou protocolos que exigem respostas dentro de um tempo limitado.
- **Funcionamento:**
    - O temporizador começa quando `accept()` é chamado.
    - Se expirar, `accept()` lança **`SocketTimeoutException`**.
    - Se `SO_TIMEOUT = 0`, o servidor espera indefinidamente.

---  

###### **Métodos para Configuração**

1. **`setSoTimeout(int timeout)`**
    - Define o tempo máximo de espera (em ms).
    - Deve ser chamado **antes** de `accept()`.
    - Lança `IllegalArgumentException` se `timeout < 0`.

   **Exemplo:**
   ```java  
   ServerSocket server = new ServerSocket(port);  
   server.setSoTimeout(30000); // 30 segundos  
   try {  
       Socket s = server.accept(); // Lança SocketTimeoutException se expirar  
   } catch (SocketTimeoutException ex) {  
       System.err.println("Nenhuma conexão em 30 segundos.");  
   }  
   ```  

2. **`getSoTimeout()`**
    - Retorna o valor atual do `SO_TIMEOUT`.

   **Exemplo:**
   ```java  
   int timeout = server.getSoTimeout();  
   if (timeout == 0) {  
       System.out.println("O servidor nunca expira.");  
   } else {  
       System.out.println("Expira em " + timeout + " ms.");  
   }  
   ```  

---  

###### **Cenários de Uso**
1. **Protocolos com Tempo Limitado:**
    - Ex.: Autenticação em duas etapas com prazo para resposta.
2. **Prevenção de Bloqueio Permanente:**
    - Evita que `accept()` trave o servidor indefinidamente.
3. **Debugging e Testes:**
    - Simula falhas de conexão em ambientes controlados.

---  

###### **Tratamento de Exceções**

- **`SocketTimeoutException`:**
    - Indica que nenhum cliente conectou dentro do tempo estipulado.
    - Pode ser usada para tentativas alternativas ou log de erros.

- **`IllegalArgumentException`:**
    - Ocorre se `timeout` for negativo.

---  

###### **Exemplo Completo**
```java  
try (ServerSocket server = new ServerSocket(8080)) {  
    server.setSoTimeout(5000); // 5 segundos  
    try {  
        Socket client = server.accept();  
        System.out.println("Conexão estabelecida!");  
    } catch (SocketTimeoutException ex) {  
        System.err.println("Nenhum cliente conectado em 5 segundos.");  
    }  
} catch (IOException e) {  
    System.err.println("Erro no servidor: " + e.getMessage());  
}  
```  

---  

###### **Conclusão**
- **`SO_TIMEOUT`** é útil para controlar o tempo de espera do `accept()`.
- **Valor `0`** (padrão) significa espera infinita.
- **Exceções** devem ser tratadas para evitar falhas inesperadas.
- Ideal para cenários que exigem **respostas rápidas** ou **tolerância a falhas**.

##### SO_REUSEADDR


A opção **`SO_REUSEADDR`** controla se um `ServerSocket` pode se vincular a uma porta que já foi usada anteriormente, mesmo que pacotes de conexões antigas ainda estejam em trânsito na rede.

###### **Principais Características**
- **Funcionamento:**
    - Permite reutilizar uma porta imediatamente após o encerramento de um socket anterior.
    - Sem essa opção, a porta fica temporariamente bloqueada (estado `TIME_WAIT`).
- **Valor padrão:** Depende do sistema operacional (no Linux e macOS, geralmente é `true`).


###### **Métodos para Configuração**

1. **`setReuseAddress(boolean on)`**
    - Ativa (`true`) ou desativa (`false`) a reutilização da porta.
    - Deve ser chamado **antes** do `bind()`.

   **Exemplo:**
   ```java  
   ServerSocket server = new ServerSocket();  
   server.setReuseAddress(true); // Permite reutilização  
   server.bind(new InetSocketAddress(8080));  
   ```  

2. **`getReuseAddress()`**
    - Retorna `true` se a reutilização estiver ativada.

   **Exemplo (verificação do padrão do sistema):**
   ```java  
   ServerSocket ss = new ServerSocket(10240);  
   System.out.println("Reutilizável? " + ss.getReuseAddress());  
   // Saída no Linux/macOS: "Reutilizável? true"  
   ```  

---  

###### **Casos de Uso Comuns**

1. **Reinício Rápido de Servidores:**
    - Evita erros como `BindException: Address already in use` após reiniciar um servidor.
2. **Testes e Desenvolvimento:**
    - Permite reexecutar um servidor na mesma porta sem esperar o `TIME_WAIT` do sistema.
3. **Protocolos com Conexões Efêmeras:**
    - Ex.: Servidores que encerram conexões frequentemente (como em jogos online).

---  

###### **Exemplo Completo**
```java  
try (ServerSocket server = new ServerSocket()) {  
    server.setReuseAddress(true); // Ativa reutilização  
    server.bind(new InetSocketAddress(12345));  

    System.out.println("SO_REUSEADDR: " + server.getReuseAddress());  
    Socket client = server.accept();  
    // ...  
} catch (IOException e) {  
    System.err.println("Erro: " + e.getMessage());  
}  
```  

---  

###### **Observações Importantes**
- **Sistemas Windows:** O comportamento pode variar (às vezes exige reinício para liberar a porta).
- **Segurança:** Não confunda com `SO_REUSEPORT` (que permite múltiplos sockets na mesma porta simultaneamente).
- **Efeito Imediato:** A configuração só afeta sockets **criados após** a chamada do método.

---  

###### **Conclusão**
- **`SO_REUSEADDR`** é essencial para evitar conflitos de portas em aplicações que reiniciam frequentemente.
- Sempre verifique o padrão do seu SO com `getReuseAddress()`.
- Use `setReuseAddress(true)` **antes do `bind()`** para garantir o efeito desejado.




##### SO_RCVBUF

A opção **`SO_RCVBUF`** define o **tamanho do buffer de recebimento** para os sockets clientes aceitos pelo `ServerSocket`.
Ela controla quantos dados podem ser armazenados temporariamente antes de serem lidos pela aplicação.

###### **Principais Características**
- **Padrão:** Definido pelo sistema operacional (geralmente entre 8KB e 64KB).
- **Impacto:**
    - **Buffer maior** → Melhor para conexões rápidas (ex.: streaming, transferência de arquivos grandes).
    - **Buffer menor** → Adequado para tráfego leve (ex.: requisições HTTP curtas).
- **Restrição:**
    - Valores acima de **64KB** devem ser configurados **antes do `bind()`**.

---

###### **Métodos de Configuração**
1. **`setReceiveBufferSize(int size)`**
    - Define o tamanho do buffer (em bytes).
    - Deve ser chamado **antes do `bind()`** para tamanhos > 64KB.

2. **`getReceiveBufferSize()`**
    - Retorna o tamanho atual do buffer.

**Exemplo:**
```java  
ServerSocket server = new ServerSocket();  

// Verifica e ajusta o buffer para 128KB se o padrão for menor  
if (server.getReceiveBufferSize() < 131072) {  
    server.setReceiveBufferSize(131072); // 128KB  
}  

server.bind(new InetSocketAddress(8000));  
```  

---

###### **Quando Usar?**
1. **Conexões de Alta Velocidade** (ex.: vídeo, FTP).
2. **Redes com Alta Latência** (ex.: satélite).
3. **Otimização de Throughput** (evita gargalos na recepção de dados).

---

###### **Observações Importantes**
- **Pós-`bind()`:** Alterações só afetam sockets **novos** (já aceitos mantêm o buffer original).
- **Limites do SO:** O sistema pode ajustar o valor para dentro de limites mínimos/máximos.
- **Efeito Real:** Depende da implementação da JVM e do sistema operacional.

---

###### **Exemplo Prático**
```java  
try (ServerSocket server = new ServerSocket()) {  
    // Configura buffer grande para streaming  
    server.setReceiveBufferSize(131072); // 128KB  
    server.bind(new InetSocketAddress(8080));  

    System.out.println("Buffer de recebimento: " + server.getReceiveBufferSize());  

    while (true) {  
        Socket client = server.accept(); // Herda o buffer configurado  
        // ...  
    }  
} catch (IOException e) {  
    System.err.println("Erro: " + e.getMessage());  
}  
```  

---

###### **Conclusão**
- **`SO_RCVBUF`** é útil para otimizar o desempenho em cenários específicos.
- Configure **antes do `bind()`** para valores acima de 64KB.
- Monitore o uso de memória, pois buffers grandes podem aumentar o consumo de recursos.


##### Class of Service

A classe `ServerSocket` permite definir **preferências de desempenho** para os sockets aceitos, baseadas em três critérios:
1. **Tempo de conexão** (`connectionTime`)
2. **Latência** (`latency`)
3. **Largura de banda** (`bandwidth`)

Essas preferências ajudam a otimizar o tráfego de acordo com o tipo de serviço, mas **não são garantias**, pois dependem do suporte da rede e da implementação da JVM.

---

###### **Classes de Tráfego TCP**
| **Classe**              | **Exemplo de Uso**            |
| ----------------------- | ----------------------------- |
| **Baixo Custo**         | Email, mensagens assíncronas  |
| **Alta Confiabilidade** | Transações bancárias          |
| **Máximo Throughput**   | Streaming de vídeo, downloads |
| **Mínimo Atraso**       | Chamadas VoIP, jogos online   |

---

###### **Método `setPerformancePreferences()`**

Define a prioridade relativa entre os critérios:
```java  
public void setPerformancePreferences(int connectionTime, int latency, int bandwidth)  
```  

###### **Como Usar**
- Os parâmetros são **valores relativos** (quanto maior, mais importante).
- Exemplo para priorizar largura de banda:
  ```java  
  // Banda (3) > Tempo de conexão (2) > Latência (1)  
  server.setPerformancePreferences(2, 1, 3);  
  ```  

---

###### **Limitações**
1. **Suporte Variável:**
    - Algumas JVMs (como Android) ignoram essas configurações.
    - Roteadores e stacks TCP nativos podem não respeitá-las.
2. **Não é Garantia:**
    - Apenas sugere prioridades ao sistema operacional.

---

###### **Exemplo Prático**
```java  
try (ServerSocket server = new ServerSocket(8080)) {  
    // Prioriza largura de banda (streaming) e reduz latência  
    server.setPerformancePreferences(1, 2, 3);  

    while (true) {  
        Socket client = server.accept(); // Herda as preferências  
        // ...  
    }  
} catch (IOException e) {  
    System.err.println("Erro: " + e.getMessage());  
}  
```  

---

###### **Quando Usar?**
- **Streaming de Vídeo:** `bandwidth = 3`, `latency = 2`.
- **Jogos Online:** `latency = 3`, `connectionTime = 2`.
- **Transferência de Arquivos:** `bandwidth = 3`, `connectionTime = 1`.

---

###### **Conclusão**
- Essa funcionalidade é útil para **ajuste fino** em aplicações sensíveis a desempenho.
- Teste em diferentes ambientes, pois o comportamento pode variar.
- Combine com outras otimizações (como `SO_RCVBUF`) para melhores resultados.



##### HTTP Servers


Este capítulo explora como criar **servidores HTTP personalizados** usando `ServerSocket` em Java, desde implementações simples até otimizadas para desempenho.

---

###### **Por que um Servidor HTTP Customizado?**
1. **Simplicidade para Casos Básicos**
    - Exemplo: Um servidor que só exibe "Em construção" não precisa de um Apache completo.
    - Java torna isso trivial com `ServerSocket`.

2. **Otimização para Tráfego Intenso**
    - Servidores especializados (ex.: para ícones/imagens) podem ser **mais rápidos** que servidores genéricos (como Apache/IIS).
    - Estratégias comuns:
        - Carregar arquivos em **memória RAM** na inicialização (evita acesso a disco a cada requisição).
        - Ignorar **logs desnecessários** para reduzir overhead.

3. **Vantagens do Java**
    - Mesmo que Java seja considerado mais lento que C/C++ (discutível em JVMs modernas), servidores HTTP são limitados por **largura de banda**, não CPU.
    - Recursos como **carregamento dinâmico de classes** e **gerenciamento de memória** se destacam em servidores web.

---

###### **Casos de Uso**
| **Tipo de Servidor**       | **Exemplo**                          |  
|----------------------------|--------------------------------------|  
| **Under Construction**     | Páginas estáticas simples.           |  
| **Otimizado para Imagens** | Serve arquivos de cache em RAM.      |  
| **Dinâmico (Servlet/JSP)** | Jetty, Tomcat (substitui CGI/ASP).   |  

---

###### **Exemplo Básico**
```java  
try (ServerSocket server = new ServerSocket(8080)) {  
    while (true) {  
        try (Socket client = server.accept();  
             PrintWriter out = new PrintWriter(client.getOutputStream())) {  
              
            // Resposta HTTP simples  
            out.println("HTTP/1.1 200 OK");  
            out.println("Content-Type: text/html");  
            out.println("\r\n");  
            out.println("<h1>Em construção!</h1>");  
        }  
    }  
}  
```  

---

###### **Quando Usar Java para Servidores Web?**
- **Conteúdo Dinâmico:** Servlets/JSP são mais eficientes que CGI/PHP tradicionais.
- **Otimizações Específicas:** Ex.: cache agressivo em memória.
- **Prototipagem Rápida:** Java oferece bibliotecas robustas para testes rápidos.

---

###### **Observações Finais**
- **Jetty/Tomcat:** Servidores Java prontos para produção.
- **Para além do Básico:** Livros como *Java Servlet Programming* (O’Reilly) exploram recursos avançados.
- **Performance:** Java compete com C em cenários reais, especialmente com otimizações.

Em resumo, servidores HTTP em Java são **viáveis desde projetos simples até alta escala**, combinando facilidade de desenvolvimento com performance competitiva.


##### A Single-File Server

Este exemplo demonstra um servidor HTTP simples que **sempre envia o mesmo arquivo**, independentemente da requisição. Chamado `SingleFileHTTPServer`, ele é configurado via linha de comando com:

- **Arquivo** a ser servido
- **Porta** (padrão: 80)
- **Codificação** (padrão: UTF-8)

---

###### **Funcionamento Principal**
1. **Inicialização**:
    - Lê o arquivo para memória (`byte[]`) durante a construção.
    - Prepara um cabeçalho HTTP com:
        - Tipo MIME (inferido automaticamente)
        - Tamanho do conteúdo
        - Codificação especificada

2. **Conexões**:
    - Usa `ServerSocket` para aceitar conexões.
    - Atende cada cliente em uma **thread separada** (via `ExecutorService`).

3. **Tratamento de Requisições**:
    - Lê apenas a primeira linha da requisição para verificar se é HTTP.
    - Envia o cabeçalho (se for HTTP/1.0+) seguido do conteúdo do arquivo.
    - Ignora o caminho da requisição (`GET /qualquer/coisa` retorna o mesmo arquivo).

---

###### **Exemplo de Uso**
```bash
java SingleFileHTTPServer arquivo.html 8080 UTF-8
```
- Servirá `arquivo.html` na porta 8080 com codificação UTF-8.

---

###### **Características Importantes**
- **Eficiência**: O arquivo é carregado **uma vez** na memória.
- **Thread Pool**: Suporta até 100 conexões simultâneas.
- **Cabeçalho Automático**: Inclui `Content-Length` e `Content-Type`.
- **Resposta HTTP Básica**: Sem tratamento de erros (404, 500, etc.).

---

###### **Saída de Exemplo (via Telnet)**
```http
GET / HTTP/1.0
HTTP/1.0 200 OK
Server: OneFile 2.0
Content-length: 959
Content-type: text/html; charset=UTF-8

<!DOCTYPE HTML>
<HTML>
...
```

---

###### **Casos de Uso**
- **Páginas "Em Construção"**: Simplicidade extrema.
- **Servir um Único Recurso Estático**: Ex.: política de privacidade em formato HTML.
- **Protótipo Rápido**: Testar clientes HTTP sem configurar servidores complexos.

---

###### **Limitações**
- Não suporta múltiplos arquivos ou rotas.
- Sem tratamento avançado de erros HTTP.
- Cabeçalho fixo (não personalizável por requisição).

Este servidor ilustra como Java pode criar soluções HTTP mínimas com poucas linhas de código, útil para cenários específicos onde simplicidade é prioritária.


##### A Redirector

Este exemplo apresenta um servidor HTTP especializado em **redirecionar requisições** para um novo endereço web, utilizando o código de status `302 FOUND`. O `Redirector` é configurado via linha de comando com:

- **URL de destino** (ex.: `http://www.novosite.com`)
- **Porta local** (padrão: 80)

---

###### **Funcionamento Principal**
1. **Inicialização**:
    - O servidor é iniciado na porta especificada.
    - Aceita conexões e, para cada uma, cria uma **thread dedicada** (`RedirectThread`).

2. **Tratamento de Requisições**:
    - Lê a primeira linha da requisição (ex.: `GET /caminho HTTP/1.1`).
    - Ignora o método (GET/POST) e extrai o caminho (`/caminho`).
    - Monta a URL de redirecionamento: `URL_destino + caminho`.

3. **Resposta HTTP**:
    - Envia cabeçalho com status `302 FOUND` e `Location` apontando para o novo endereço.
    - Inclui HTML de fallback para navegadores antigos que não redirecionam automaticamente.

---

###### **Exemplo de Uso**
```bash
java Redirector http://www.novosite.com 80
```
- Redireciona todas as requisições da porta 80 para `http://www.novosite.com`.

---

###### **Saída de Exemplo (via Telnet)**
```http
GET / HTTP/1.0
HTTP/1.0 302 FOUND
Date: [data_atual]
Server: Redirector 1.1
Location: http://www.novosite.com/
Content-type: text/html

<HTML><HEAD><TITLE>Document moved</TITLE></HEAD>
<BODY>...
The document / has moved to <A HREF="http://www.novosite.com/">aqui</A>.
</BODY></HTML>
```

---

###### **Casos de Uso**
- **Migração de Domínio**: Redirecionar `siteantigo.com` para `novosite.com`.
- **Consolidação de URLs**: Unificar acessos (ex.: `http://dominio` → `http://www.dominio.com`).
- **Cenários de Manutenção**: Direcionar temporariamente para uma página alternativa.

---

###### **Características Importantes**
- **Thread por Conexão**: Simples, mas menos eficiente que um *thread pool*.
- **Compatibilidade**:
    - Navegadores modernos seguem automaticamente o redirecionamento (`302`).
    - Navegadores antigos exibem o HTML com link clicável.
- **Cabeçalhos HTTP**:
    - Inclui `Location` obrigatório e metadados opcionais (`Date`, `Server`).

---

###### **Limitações**
- **Sem Suporte a HTTPS**: Redireciona apenas HTTP.
- **Sem Logs Detalhados**: Apenas registra endereços redirecionados.
- **Ineficiência em Alta Carga**: Criar uma thread por conexão pode sobrecarregar o sistema.

---

###### **Destaques do Código**
1. **Tratamento da URL**:
   ```java
   if (theSite.endsWith("/")) {
       theSite = theSite.substring(0, theSite.length() - 1);
   }
   ```
   Remove barras finais para evitar URLs como `http://site.com//caminho`.

2. **Montagem da Resposta**:
   ```java
   out.write("HTTP/1.0 302 FOUND\r\n");
   out.write("Location: " + newSite + theFile + "\r\n");
   ```

3. **Fallback para HTML**:
   ```java
   out.write("<A HREF=\"" + newSite + theFile + "\">" + newSite + theFile + "</A>");
   ```

---

###### **Conclusão**

O `Redirector` é uma solução elegante para redirecionamentos HTTP, demonstrando como Java pode criar servidores especializados com poucas linhas de código. Ideal para cenários simples, mas requer adaptações (como uso de *thread pools*) para produção em larga escala.


##### A Full-Fledged HTTP Server

Este exemplo demonstra um servidor HTTP completo que serve arquivos estáticos de um diretório raiz, incluindo HTML, imagens e outros recursos. O servidor usa um *thread pool* para lidar com múltiplas conexões simultaneamente.

###### **Classe Principal (`JHTTP`)**

```java
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class JHTTP {
    private static final Logger logger = Logger.getLogger(JHTTP.class.getCanonicalName());
    private static final int NUM_THREADS = 50;
    private static final String INDEX_FILE = "index.html";
    private final File rootDirectory;
    private final int port;

    public JHTTP(File rootDirectory, int port) throws IOException {
        if (!rootDirectory.isDirectory()) {
            throw new IOException(rootDirectory + " não é um diretório válido.");
        }
        this.rootDirectory = rootDirectory.getCanonicalFile();
        this.port = port;
    }

    public void start() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("Servidor rodando na porta " + server.getLocalPort());
            logger.info("Diretório raiz: " + rootDirectory);

            while (true) {
                try {
                    Socket request = server.accept();
                    Runnable r = new RequestProcessor(rootDirectory, INDEX_FILE, request);
                    pool.submit(r);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Erro ao aceitar conexão", ex);
                }
            }
        }
    }

    public static void main(String[] args) {
        // Configura diretório raiz
        File docroot;
        try {
            docroot = new File(args[0]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Uso: java JHTTP <diretório-raiz> <porta>");
            return;
        }

        // Configura porta
        int port;
        try {
            port = Integer.parseInt(args[1]);
            if (port < 0 || port > 65535) port = 80;
        } catch (RuntimeException ex) {
            port = 80;
        }

        try {
            JHTTP webserver = new JHTTP(docroot, port);
            webserver.start();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Falha ao iniciar servidor", ex);
        }
    }
}
```

---

###### **Classe `RequestProcessor` (Tratamento de Requisições)**

```java
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;

public class RequestProcessor implements Runnable {
    private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());
    private final File rootDirectory;
    private final String indexFileName;
    private final Socket connection;

    public RequestProcessor(File rootDirectory, String indexFileName, Socket connection) {
        this.rootDirectory = rootDirectory;
        this.indexFileName = indexFileName;
        this.connection = connection;
    }

    @Override
    public void run() {
        String root = rootDirectory.getPath();
        try {
            OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
            Writer out = new OutputStreamWriter(raw);
            Reader in = new InputStreamReader(new BufferedInputStream(connection.getInputStream()), "US-ASCII");

            // Lê a primeira linha da requisição (ex: "GET /file.html HTTP/1.1")
            StringBuilder requestLine = new StringBuilder();
            while (true) {
                int c = in.read();
                if (c == '\r' || c == '\n') break;
                requestLine.append((char) c);
            }

            String request = requestLine.toString();
            logger.info(connection.getRemoteSocketAddress() + " " + request);

            String[] tokens = request.split("\\s+");
            String method = tokens[0];
            String fileName = tokens[1];
            String version = tokens.length > 2 ? tokens[2] : "";

            // Trata apenas requisições GET
            if (method.equals("GET")) {
                // Adiciona "index.html" se for um diretório
                if (fileName.endsWith("/")) fileName += indexFileName;

                // Obtém tipo MIME (ex: "text/html")
                String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);

                // Verifica se o arquivo existe e está dentro do diretório raiz
                File theFile = new File(rootDirectory, fileName.substring(1));
                if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) {
                    byte[] fileData = Files.readAllBytes(theFile.toPath());
                    if (version.startsWith("HTTP/")) {
                        sendHeader(out, "HTTP/1.0 200 OK", contentType, fileData.length);
                    }
                    raw.write(fileData); // Envia o arquivo
                } else {
                    // Arquivo não encontrado (404)
                    String errorBody = "<HTML><HEAD><TITLE>404 Not Found</TITLE></HEAD><BODY>404</BODY></HTML>";
                    if (version.startsWith("HTTP/")) {
                        sendHeader(out, "HTTP/1.0 404 Not Found", "text/html", errorBody.length());
                    }
                    out.write(errorBody);
                }
            } else {
                // Método não suportado (501)
                String errorBody = "<HTML><HEAD><TITLE>501 Not Implemented</TITLE></HEAD><BODY>501</BODY></HTML>";
                if (version.startsWith("HTTP/")) {
                    sendHeader(out, "HTTP/1.0 501 Not Implemented", "text/html", errorBody.length());
                }
                out.write(errorBody);
            }
            out.flush();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Erro na conexão", ex);
        } finally {
            try {
                connection.close();
            } catch (IOException ignored) {}
        }
    }

    private void sendHeader(Writer out, String status, String contentType, int length) throws IOException {
        out.write(status + "\r\n");
        out.write("Server: JHTTP 2.0\r\n");
        out.write("Content-Type: " + contentType + "\r\n");
        out.write("Content-Length: " + length + "\r\n\r\n");
        out.flush();
    }
}
```

---

###### **Funcionamento**
1. **Inicialização**:
    - Define um diretório raiz (`docroot`) e uma porta (padrão: `80`).
    - Cria um *thread pool* para lidar com conexões simultâneas.

2. **Tratamento de Requisições**:
    - Aceita conexões e delega cada uma para um `RequestProcessor`.
    - Lê a linha `GET /arquivo HTTP/1.1` e extrai o caminho do arquivo.
    - Se o caminho terminar com `/`, adiciona `index.html`.
    - Verifica se o arquivo existe e está dentro do diretório raiz (evita ataques como `../../../`).

3. **Respostas**:
    - **200 OK**: Arquivo encontrado, envia cabeçalho + conteúdo.
    - **404 Not Found**: Arquivo não existe.
    - **501 Not Implemented**: Método HTTP não suportado (ex: POST).

---

###### **Melhorias Possíveis**
- **Cache em Memória**: Armazenar arquivos frequentemente acessados para reduzir I/O.
- **Suporte a HTTPS**: Adicionar SSL/TLS.
- **Logs Detalhados**: Registrar IPs, horários e arquivos acessados.
- **Suporte a POST/PUT**: Para envio de formulários e uploads.

---

###### **Como Usar?**
```bash
java JHTTP /var/www 8080
```
- Servirá arquivos de `/var/www` na porta `8080`.

---

###### **Conclusão**
Este servidor é **funcional para uso básico**, mas pode ser estendido para cenários mais complexos. Ideal para aprendizado e prototipagem rápida. 

