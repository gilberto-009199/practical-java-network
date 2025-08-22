## Secure Sockets

###### **Problema: Vigilância na Internet**
- Agências governamentais (como NSA, GCHQ) **monitoram tráfego de dados** em larga escala.
- Empresas de telecomunicações fornecem acesso a **dados de usuários** sem consentimento.
- **Fibra óptica e cabos submarinos** são interceptados para coleta massiva de informações.

###### **Solução: Criptografia com Sockets Seguros**

Para proteger comunicações na Internet, é possível usar **sockets criptografados**, garantindo:
1. **Confidencialidade**: Dados só são acessíveis por destinatários autorizados.
2. **Autenticação**: Verificação da identidade de quem envia/recebe.
3. **Integridade**: Garantia de que os dados não foram alterados no trajeto.

---

###### **Desafios da Criptografia**

- **Complexidade**: Exige conhecimento avançado em algoritmos e protocolos.
- **Riscos**: Pequenos erros podem comprometer toda a segurança.
- **Solução Prática**: Usar bibliotecas desenvolvidas por especialistas.

---

###### **Java Secure Sockets Extension (JSSE)**

O JSSE permite comunicações seguras em Java usando:
- **SSL (Secure Sockets Layer) v3**
- **TLS (Transport Layer Security)**

###### **Funcionalidades Principais**
- **Criptografia de Dados**: Transforma informações em formato ilegível para interceptadores.
- **Autenticação de Servidor/Cliente**: Certificados digitais verificam identidades.
- **Suporte a Protocolos**: HTTPS, FTPS, etc.

---

###### **Como Funciona na Prática?**
1. **Cliente** (navegador, app) e **servidor** (loja online) negociam uma conexão segura.
2. **Handshake SSL/TLS**:
    - Troca de certificados.
    - Definição de algoritmos de criptografia.
3. **Dados trafegam criptografados**.

###### **Exemplo de Uso em Java**
```java
// Cria um socket SSL seguro para HTTPS
SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
SSLSocket socket = (SSLSocket) factory.createSocket("example.com", 443);

// Configura protocolos (TLS 1.2 ou superior)
socket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});

// Inicia a comunicação segura
OutputStream out = socket.getOutputStream();
out.write("GET / HTTP/1.1\r\nHost: example.com\r\n\r\n".getBytes());
```

---

###### **Por Que Usar JSSE?**
- **Evita "espiões"**: Dados ficam ilegíveis para interceptadores.
- **Protege Senhas/Cartões**: Transações online são seguras.
- **Fácil Integração**: Não é preciso implementar criptografia manualmente.

---

###### **Conclusão**

A JSSE simplifica a **segurança em aplicações Java**, permitindo que desenvolvedores usem **SSL/TLS** sem precisar dominar todos os detalhes matemáticos da criptografia. É essencial para:
- **Lojas online**
- **Aplicações bancárias**
- **Qualquer sistema que trafegue dados sensíveis**.

>  **Dica**: Sempre use **TLS 1.2 ou superior** (SSLv3 é considerado inseguro hoje).


##### Secure Communications

- Agências governamentais (como NSA, GCHQ) **monitoram tráfego de dados** em larga escala.
- Empresas de telecomunicações fornecem acesso a **dados de usuários** sem consentimento.
- **Fibra óptica e cabos submarinos** são interceptados para coleta massiva de informações.

###### **Solução: Criptografia com Sockets Seguros**

Para proteger comunicações na Internet, é possível usar **sockets criptografados**, garantindo:
1. **Confidencialidade**: Dados só são acessíveis por destinatários autorizados.
2. **Autenticação**: Verificação da identidade de quem envia/recebe.
3. **Integridade**: Garantia de que os dados não foram alterados no trajeto.

---

###### **Desafios da Criptografia**

- **Complexidade**: Exige conhecimento avançado em algoritmos e protocolos.
- **Riscos**: Pequenos erros podem comprometer toda a segurança.
- **Solução Prática**: Usar bibliotecas desenvolvidas por especialistas.

---

###### **Java Secure Sockets Extension (JSSE)**

O JSSE permite comunicações seguras em Java usando:
- **SSL (Secure Sockets Layer) v3**
- **TLS (Transport Layer Security)**

###### **Funcionalidades Principais**
- **Criptografia de Dados**: Transforma informações em formato ilegível para interceptadores.
- **Autenticação de Servidor/Cliente**: Certificados digitais verificam identidades.
- **Suporte a Protocolos**: HTTPS, FTPS, etc.

---

###### **Como Funciona na Prática?**
1. **Cliente** (navegador, app) e **servidor** (loja online) negociam uma conexão segura.
2. **Handshake SSL/TLS**:
    - Troca de certificados.
    - Definição de algoritmos de criptografia.
3. **Dados trafegam criptografados**.

###### **Exemplo de Uso em Java**
```java
// Cria um socket SSL seguro para HTTPS
SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
SSLSocket socket = (SSLSocket) factory.createSocket("example.com", 443);

// Configura protocolos (TLS 1.2 ou superior)
socket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});

// Inicia a comunicação segura
OutputStream out = socket.getOutputStream();
out.write("GET / HTTP/1.1\r\nHost: example.com\r\n\r\n".getBytes());
```

---

###### **Por Que Usar JSSE?**
- **Evita "espiões"**: Dados ficam ilegíveis para interceptadores.
- **Protege Senhas/Cartões**: Transações online são seguras.
- **Fácil Integração**: Não é preciso implementar criptografia manualmente.

A JSSE simplifica a **segurança em aplicações Java**, permitindo que desenvolvedores usem **SSL/TLS** sem precisar dominar todos os detalhes matemáticos da criptografia. É essencial para:
- **Lojas online**
- **Aplicações bancárias**
- **Qualquer sistema que trafegue dados sensíveis**.

> 💡 **Dica**: Sempre use **TLS 1.2 ou superior** (SSLv3 é considerado inseguro hoje).


> Livro, pagina 52
> A criptografia assimétrica também pode ser usada para autenticação e verificação da integridade de mensagens. Para esse uso, Angela criptografava uma mensagem com sua chave privada antes de enviá-la. Quando Gus a recebia, ele a descriptografava com a chave pública de Angela. Se a descriptografia fosse bem-sucedida, Gus saberia que a mensagem veio de Angela. Afinal, ninguém mais poderia ter produzido uma mensagem que fosse descriptografada corretamente com sua chave pública.
>
> chave. Gus também saberia que a mensagem não foi alterada no caminho, seja maliciosamente por Edgar ou involuntariamente por software com bugs ou ruído de rede, porque qualquer alteração desse tipo teria prejudicado a descriptografia. Com um pouco mais de esforço, Angela pode criptografar a mensagem duas vezes, uma com sua chave privada e outra com a chave pública de Gus, obtendo assim os três benefícios de privacidade, autenticação e integridade.
>
> Na prática, a criptografia de chave pública exige muito mais da CPU e é muito mais lenta do que a criptografia de chave secreta. Portanto, em vez de criptografar toda a transmissão com a chave pública de Gus, Angela criptografa uma chave secreta tradicional e a envia para Gus. Gus a descriptografa com sua chave privada. Agora, Angela e Gus conhecem a chave secreta, mas Edgar não. Portanto, Gus e Angela agora podem usar uma criptografia de chave secreta mais rápida para se comunicarem em particular, sem que Edgar esteja ouvindo.

##### Creating Secure Client Sockets

Se você não se importa muito com os detalhes subjacentes, usar um soquete SSL criptografado para se comunicar com um servidor seguro existente é realmente simples. Em vez de construir um objeto `java.net.Socket` com um construtor, você obtém um de uma `javax.net.ssl.SSLSocketFactory` usando seu método `createSocket()`. A `SSLSocketFactory` é uma classe abstrata que segue o padrão de projeto *abstract factory*. Você obtém uma instância dela invocando o método estático `SSLSocketFactory.getDefault()`:

```java
SocketFactory factory = SSLSocketFactory.getDefault();
Socket socket = factory.createSocket("login.ibiblio.org", 7000);
```  

Isso retorna uma instância de `SSLSocketFactory` ou lança uma `InstantiationException` se nenhuma subclasse concreta for encontrada. Uma vez que você tem uma referência à fábrica, use um desses cinco métodos sobrecarregados `createSocket()` para construir um `SSLSocket`:

```java
public abstract Socket createSocket(String host, int port)  
    throws IOException, UnknownHostException  

public abstract Socket createSocket(InetAddress host, int port)  
    throws IOException  

public abstract Socket createSocket(String host, int port,  
    InetAddress interface, int localPort)  
    throws IOException, UnknownHostException  

public abstract Socket createSocket(InetAddress host, int port,  
    InetAddress interface, int localPort)  
    throws IOException, UnknownHostException  

public abstract Socket createSocket(Socket proxy, String host, int port,  
    boolean autoClose) throws IOException  
```  

Os dois primeiros métodos criam e retornam um soquete conectado ao *host* e porta especificados ou lançam uma `IOException` se não conseguirem se conectar. O terceiro e quarto métodos conectam e retornam um soquete vinculado à interface de rede local e porta especificadas, enquanto se comunicam com o *host* e porta remotos. O último método `createSocket()`, no entanto, é um pouco diferente. Ele começa com um objeto `Socket` existente conectado a um servidor proxy e retorna um `Socket` que faz um *tunnel* através desse proxy para o *host* e porta especificados. O argumento `autoClose` determina se o soquete proxy subjacente deve ser fechado quando este soquete for fechado. Se `autoClose` for `true`, o soquete subjacente será fechado; se `false`, não será.

O `Socket` retornado por esses métodos será, na verdade, um `javax.net.ssl.SSLSocket`, uma subclasse de `java.net.Socket`. No entanto, você não precisa saber disso. Uma vez que o soquete seguro é criado, você o usa como qualquer outro soquete, através de seus métodos `getInputStream()`, `getOutputStream()` e outros.

Por exemplo, suponha que um servidor que aceita pedidos esteja escutando na porta 7000 de `login.ibiblio.org`. Cada pedido é enviado como uma string ASCII usando uma única conexão TCP. O servidor aceita o pedido e fecha a conexão. (Estou omitindo muitos detalhes que seriam necessários em um sistema real, como o servidor enviar um código de resposta informando se o pedido foi aceito.) Os pedidos enviados pelos clientes têm esta aparência:

```
Name: John Smith  
Product-ID: 67X-89  
Address: 1280 Deniston Blvd, NY NY 10003  
Card number: 4000-1234-5678-9017  
Expires: 08/05  
```  

Há informações suficientes nessa mensagem para que alguém bisbilhotando os pacotes possa usar o cartão de crédito de John Smith para fins maliciosos. Portanto, antes de enviar esse pedido, você deve criptografá-lo. A maneira mais simples de fazer isso sem sobrecarregar o servidor ou o cliente com um código de criptografia complexo e propenso a erros é usar um soquete seguro. O código a seguir envia o pedido por um soquete seguro:

```java
SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();  
Socket socket = factory.createSocket("login.ibiblio.org", 7000);  

Writer out = new OutputStreamWriter(socket.getOutputStream(), "US-ASCII");  
out.write("Name: John Smith\r\n");  
out.write("Product-ID: 67X-89\r\n");  
out.write("Address: 1280 Deniston Blvd, NY NY 10003\r\n");  
out.write("Card number: 4000-1234-5678-9017\r\n");  
out.write("Expires: 08/05\r\n");  
out.flush();  
```  

Apenas as três primeiras instruções no bloco `try` são visivelmente diferentes do que você faria com um soquete inseguro. O resto do código simplesmente usa os métodos normais das classes `Socket`, `OutputStream` e `Writer`.

Ler a entrada não é mais difícil. O Exemplo 10-1 é um programa simples que se conecta a um servidor HTTP seguro, envia uma requisição GET simples e imprime a resposta.

```java
import java.io.*;  
import javax.net.ssl.*;  

public class HTTPSClient {  
    public static void main(String[] args) {  
        if (args.length == 0) {  
            System.out.println("Usage: java HTTPSClient2 host");  
            return;  
        }  

        int port = 443; // porta HTTPS padrão  
        String host = args[0];  

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();  
        SSLSocket socket = null;  

        try {  
            socket = (SSLSocket) factory.createSocket(host, port);  

            // habilita todos os conjuntos de cifras  
            String[] supported = socket.getSupportedCipherSuites();  
            socket.setEnabledCipherSuites(supported);  

            Writer out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");  
            // HTTPS requer a URL completa na linha GET  
            out.write("GET http://" + host + "/ HTTP/1.1\r\n");  
            out.write("Host: " + host + "\r\n");  
            out.write("\r\n");  
            out.flush();  

            // lê a resposta  
            BufferedReader in = new BufferedReader(  
                new InputStreamReader(socket.getInputStream()));  

            // lê o cabeçalho  
            String s;  
            while (!(s = in.readLine()).equals("")) {  
                System.out.println(s);  
            }  
            System.out.println();  

            // lê o comprimento  
            String contentLength = in.readLine();  
            int length = Integer.MAX_VALUE;  
            try {  
                length = Integer.parseInt(contentLength.trim(), 16);  
            } catch (NumberFormatException ex) {  
                // Este servidor não envia o content-length na primeira linha do corpo da resposta  
            }  

            System.out.println(contentLength);  

            int c;  
            int i = 0;  
            while ((c = in.read()) != -1 && i++ < length) {  
                System.out.write(c);  
            }  
            System.out.println();  

        } catch (IOException ex) {  
            System.err.println(ex);  
        } finally {  
            try {  
                if (socket != null) socket.close();  
            } catch (IOException e) {}  
        }  
    }  
}  
```  

Aqui estão as primeiras linhas de saída deste programa ao se conectar ao site do Serviço Postal dos EUA:

```
% java HTTPSClient www.usps.com  
HTTP/1.1 200 OK  
Server: IBM_HTTP_Server  
Cache-Control: max-age=0  
Expires: Sun, 31 Mar 2013 17:29:33 GMT  
Content-Type: text/html  
Date: Sun, 31 Mar 2013 18:00:14 GMT  
Transfer-Encoding: chunked  
Connection: keep-alive  
Connection: Transfer-Encoding  
00004000  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"  
"http://www.w3.org/TR/html4/loose.dtd">  
```  

Quando testei este programa para a edição anterior, ele inicialmente se recusou a se conectar a `www.usps.com` porque não conseguia verificar a identidade do servidor remoto. O problema era que os certificados raiz incluídos na versão do JDK que eu estava usando (1.4.2_02-b3) haviam expirado. Atualizar para a última versão secundária (1.4.2_03-b2) resolveu o problema. Se você vir mensagens de exceção como "Nenhum certificado confiável encontrado", tente atualizar para a última versão secundária do JDK.

Ao executar este programa, você pode notar que ele é mais lento do que o esperado. Há uma sobrecarga significativa de CPU e rede envolvida na geração e troca de chaves públicas. Mesmo em uma rede rápida, pode levar alguns segundos para estabelecer uma conexão. Por isso, talvez você não queira servir todo o seu conteúdo por HTTPS, apenas o conteúdo que realmente precisa ser privado e não é sensível à latência.



###### Choosing the Cipher Suites


Diferentes implementações do JSSE (Java Secure Socket Extension) suportam diferentes combinações de algoritmos de autenticação e criptografia. Por exemplo, a implementação que a Oracle inclui no Java 7 suporta apenas criptografia AES de 128 bits, enquanto a implementação iSaSiLk da IAIK suporta criptografia AES de 256 bits.

A implementação padrão do JSSE que vem com o JDK realmente possui código para criptografia mais forte (256 bits), mas ela está desativada, a menos que você instale os **JCE Unlimited Strength Jurisdiction Policy Files**. Nem vou tentar explicar o emaranhado legal que torna isso necessário.

O método `getSupportedCipherSuites()` da `SSLSocketFactory` informa quais combinações de algoritmos estão disponíveis em um soquete específico:

```java
public abstract String[] getSupportedCipherSuites()  
```  

No entanto, nem todos os conjuntos de cifras suportados estão necessariamente habilitados na conexão. Alguns podem ser considerados muito fracos e, portanto, desativados. O método `getEnabledCipherSuites()` da `SSLSocketFactory` informa quais conjuntos o soquete está disposto a usar:

```java
public abstract String[] getEnabledCipherSuites()  
```  

O conjunto de cifras realmente usado é negociado entre o cliente e o servidor no momento da conexão. É possível que o cliente e o servidor não concordem em nenhum conjunto. Também pode acontecer de, mesmo que um conjunto esteja habilitado em ambos, um dos lados (ou ambos) não tenha as chaves ou certificados necessários para usá-lo. Em qualquer desses casos, o método `createSocket()` lançará uma `SSLException` (uma subclasse de `IOException`).

Você pode alterar os conjuntos de cifras que o cliente tenta usar através do método `setEnabledCipherSuites()`:

```java
public abstract void setEnabledCipherSuites(String[] suites)  
```  

O argumento desse método deve ser uma lista dos conjuntos que você deseja usar. Cada nome deve ser um dos listados por `getSupportedCipherSuites()`. Caso contrário, uma `IllegalArgumentException` será lançada.

O JDK 1.7 da Oracle suporta os seguintes conjuntos de cifras:

*(Lista dos conjuntos de cifras omitida para brevidade, mas inclui combinações como TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256, TLS_RSA_WITH_AES_128_CBC_SHA, SSL_RSA_WITH_RC4_128_MD5, entre outros.)*

Cada nome segue um padrão de quatro partes: **protocolo**, **algoritmo de troca de chaves**, **algoritmo de criptografia** e **checksum**. Por exemplo, o nome `SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA` significa:

- **SSL**: Secure Sockets Layer versão 3
- **DH_anon**: Troca de chaves via Diffie-Hellman sem autenticação
- **DES40_CBC**: Criptografia DES com chaves de 40 bits no modo Cipher Block Chaining
- **SHA**: Checksum usando Secure Hash Algorithm

Por padrão, a implementação do JDK 1.7 habilita todos os conjuntos **autenticados e criptografados** (os primeiros 28 da lista). Se você quiser conexões não autenticadas ou autenticadas mas não criptografadas, precisará habilitá-las explicitamente com `setEnabledCipherSuites()`. **Evite** conjuntos que contenham `NULL`, `ANON` ou `EXPORT` no nome, a menos que queira que a NSA leia suas mensagens.

O conjunto `TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256` é considerado seguro contra todos os ataques conhecidos. `TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA256` é ainda melhor, se estiver habilitado. Em geral, qualquer conjunto que comece com `TLS_ECDHE` e termine com `SHA256` ou `SHA384` oferece a criptografia mais forte disponível hoje. A maioria dos outros está sujeita a ataques de diferentes níveis de gravidade.

Além do tamanho das chaves, há uma diferença importante entre cifras baseadas em **DES/AES** e **RC4**:

- **DES e AES** são cifras de bloco (criptografam um número fixo de bits por vez).
    - DES sempre criptografa blocos de 64 bits.
    - AES pode usar blocos de 128, 192 ou 256 bits.
    - Ambos exigem **preenchimento (padding)** se os dados não forem múltiplos do tamanho do bloco.
- **RC4** é uma cifra de fluxo (pode criptografar um byte por vez), sendo mais adequada para protocolos como chat e Telnet, onde dados podem ser enviados byte a byte.

Suponha que Edgar tenha computadores poderosos e consiga quebrar criptografia de 64 bits ou menos. Gus e Angela, sabendo disso, querem evitar conexões anônimas (vulneráveis a ataques *man-in-the-middle*). Eles decidem usar apenas o conjunto mais forte disponível:

```java
String[] strongSuites = {"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256"};  
socket.setEnabledCipherSuites(strongSuites);  
```  

Se o outro lado não suportar esse protocolo, o soquete lançará uma exceção ao tentar ler/escrever, garantindo que nenhuma informação trafegue por um canal inseguro.



###### Event Handlers

As comunicações de rede são lentas em comparação com a velocidade da maioria dos computadores.

Comunicações de rede autenticadas são ainda mais lentas. A geração de chaves e configuração necessária para uma conexão segura pode facilmente levar vários segundos. Por isso, você pode querer lidar com a conexão de forma assíncrona.

O JSSE (Java Secure Socket Extension) utiliza o modelo de eventos padrão do Java para notificar programas quando o handshake (aperto de mão) entre cliente e servidor é concluído. O padrão é familiar:

Para receber notificações de eventos de handshake concluído, basta implementar a interface **HandshakeCompletedListener**:

```java
public interface HandshakeCompletedListener extends java.util.EventListener
```  

Essa interface declara o método **handshakeCompleted()**:

```java
public void handshakeCompleted(HandshakeCompletedEvent event)
```  

Esse método recebe como argumento um **HandshakeCompletedEvent**:

```java
public class HandshakeCompletedEvent extends java.util.EventObject
```  

A classe **HandshakeCompletedEvent** fornece quatro métodos para obter informações sobre o evento:

```java
public SSLSession getSession()  
public String getCipherSuite()  
public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException  
public SSLSocket getSocket()  
```  

Objetos específicos que implementam **HandshakeCompletedListener** registram seu interesse em eventos de handshake concluído em um **SSLSocket** específico por meio dos métodos:

```java
public abstract void addHandshakeCompletedListener(HandshakeCompletedListener listener)  
public abstract void removeHandshakeCompletedListener(HandshakeCompletedListener listener) throws IllegalArgumentException  
```  

Funcionamento:
1. Um **HandshakeCompletedListener** é registrado em um **SSLSocket** usando `addHandshakeCompletedListener()`.
2. Quando o handshake SSL/TLS é concluído, o método `handshakeCompleted()` é chamado, passando um **HandshakeCompletedEvent** com detalhes como:
    - A sessão SSL (`getSession()`).
    - O conjunto de cifras usado (`getCipherSuite()`).
    - A cadeia de certificados do servidor (`getPeerCertificateChain()`).
    - O próprio soquete (`getSocket()`).
3. Se o listener não for mais necessário, ele pode ser removido com `removeHandshakeCompletedListener()`.

```java
socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {  
    @Override  
    public void handshakeCompleted(HandshakeCompletedEvent event) {  
        System.out.println("Handshake concluído!");  
        System.out.println("Cifra usada: " + event.getCipherSuite());  
    }  
});  
```  

Isso permite que a aplicação continue operando sem bloquear enquanto o handshake SSL ocorre em segundo plano, melhorando a responsividade.

###### Session Management


O SSL é comumente usado em servidores web, e com boa razão. Conexões web tendem a ser transitórias - cada página requer um soquete separado. Por exemplo, finalizar uma compra no Amazon.com em seu servidor seguro requer sete carregamentos de página distintos, mais ainda se for necessário editar um endereço ou selecionar embrulho para presente. Imagine se cada uma dessas páginas levasse 10 segundos adicionais ou mais para negociar uma conexão segura.

Devido à alta sobrecarga envolvida no handshake (aperto de mão) entre dois hosts para comunicações seguras, o SSL permite que sessões sejam estabelecidas e estendidas por múltiplos soquetes. Diferentes soquetes dentro da mesma sessão usam o mesmo conjunto de chaves públicas e privadas. Se a conexão segura com a Amazon.com usar sete soquetes, todos os sete serão estabelecidos dentro da mesma sessão e usarão as mesmas chaves. Apenas o primeiro soquete dentro dessa sessão terá que arcar com a sobrecarga de geração e troca de chaves.

Uso Prático com JSSE

Como programador usando JSSE, você não precisa fazer nada extra para aproveitar as sessões. Se você abrir múltiplos soquetes seguros para um mesmo host e porta dentro de um período razoavelmente curto, o JSSE reutilizará automaticamente as chaves da sessão.

No entanto, em aplicações de alta segurança, você pode querer:
- **Impedir o compartilhamento de sessão** entre soquetes
- **Forçar a reautenticação** de uma sessão

No JSSE, as sessões são representadas por instâncias da interface **SSLSession**. Você pode usar seus métodos para:
- Verificar quando a sessão foi criada (`getCreationTime()`)
- Verificar o último acesso (`getLastAccessedTime()`)
- Invalidar a sessão (`invalidate()`)
- Armazenar/recuperar valores associados à sessão (`putValue()`, `getValue()`)
- Obter informações sobre a sessão (certificados, cifra usada, etc.)

Controle Programático

O método `getSession()` de **SSLSocket** retorna a sessão à qual o soquete pertence:
```java
public abstract SSLSession getSession()
```  

No entanto, sessões representam um equilíbrio entre **performance** e **segurança**. É mais seguro renegociar a chave para cada transação. Se você tiver hardware poderoso e estiver protegendo sistemas contra um adversário igualmente determinado, rico e competente, pode querer evitar sessões.

Para impedir que um soquete crie uma sessão:
```java
public abstract void setEnableSessionCreation(boolean allowSessions)
```  
(passe `false` como argumento)

Para verificar se a criação de sessões está habilitada:
```java
public abstract boolean getEnableSessionCreation()
```  

Em casos raros, você pode querer **reautenticar uma conexão** (descartando todos os certificados e chaves previamente acordados e iniciando uma nova sessão). O método `startHandshake()` faz isso:
```java
public abstract void startHandshake() throws IOException
```  

Exemplo de Uso
```java
// Desabilita sessões compartilhadas para maior segurança
socket.setEnableSessionCreation(false);

// Força reautenticação
socket.startHandshake();

// Obtém informações da sessão atual
SSLSession session = socket.getSession();
System.out.println("Cifra usada: " + session.getCipherSuite());
System.out.println("Host remoto: " + session.getPeerHost());
```  

Isso permite balancear segurança e desempenho conforme necessário para sua aplicação.


##### Client Mode

Em geral, na maioria das comunicações seguras, o servidor é obrigado a se autenticar usando o certificado apropriado, enquanto o cliente não. Por exemplo, quando compro um livro na Amazon através de seu servidor seguro, ele precisa provar para meu navegador que realmente é a Amazon e não um hacker qualquer. Porém, eu não preciso provar para a Amazon que sou Elliotte Rusty Harold.

Na maioria dos casos, isso faz sentido, já que adquirir e instalar os certificados confiáveis necessários para autenticação é uma experiência complicada para usuários comuns - algo que clientes não deveriam precisar enfrentar apenas para comprar um livro. No entanto, essa assimetria pode facilitar fraudes com cartões de crédito.

Para evitar esse tipo de problema, soquetes podem ser configurados para exigir autenticação mútua. Essa abordagem não seria prática para serviços abertos ao público em geral, mas pode ser útil em aplicações internas de alta segurança.

Configurando Autenticação

O método `setUseClientMode()` define se o soquete deve se autenticar durante o primeiro handshake (aperto de mão). O nome do método pode ser confuso:

- **true (modo cliente)**: O soquete não oferecerá autenticação (padrão)
- **false**: O soquete tentará se autenticar

```java
public abstract void setUseClientMode(boolean mode) throws IllegalArgumentException
```  

Essa configuração só pode ser definida **uma vez** por soquete. Tentar alterá-la novamente lança uma `IllegalArgumentException`.

Para verificar o modo atual:
```java
public abstract boolean getUseClientMode()
```  

No Lado do Servidor

Soquetes seguros no servidor (retornados pelo método `accept()` de um `SSLServerSocket`) usam `setNeedClientAuth()` para exigir (ou não) autenticação dos clientes:

```java
public abstract void setNeedClientAuth(boolean needsAuthentication) throws IllegalArgumentException
```  

(Se o soquete não for do lado servidor, lança `IllegalArgumentException`)

Para verificar a configuração:
```java
public abstract boolean getNeedClientAuth()
```  

Exemplo de Uso
```java
// Configuração do cliente (opcionalmente exigindo autenticação)
SSLSocket clientSocket = (SSLSocket) factory.createSocket("servidor", 443);
clientSocket.setUseClientMode(false); // Habilita autenticação do cliente

// Configuração do servidor (exigindo autenticação do cliente)
SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(443);
serverSocket.setNeedClientAuth(true); // Obrigatório autenticação
```  

Esses métodos permitem implementar desde comunicações padrão (apenas servidor autenticado) até esquemas de alta segurança com autenticação mútua.


##### Creating Secure Server Sockets

Soquetes seguros do lado do cliente são apenas metade da equação. A outra metade são os soquetes de servidor habilitados para SSL, representados pela classe `javax.net.SSLServerSocket`:

```java
public abstract class SSLServerSocket extends ServerSocket
```

Assim como `SSLSocket`, todos os construtores desta classe são protegidos e as instâncias são criadas por uma classe fábrica abstrata, `javax.net.SSLServerSocketFactory`:

```java
public abstract class SSLServerSocketFactory extends ServerSocketFactory
```

Obtendo a Fábrica Padrão

Assim como `SSLSocketFactory`, uma instância de `SSLServerSocketFactory` é retornada por um método estático:

```java
public static ServerSocketFactory getDefault()
```

Criando Soquetes de Servidor

A `SSLServerSocketFactory` possui três métodos sobrecarregados `createServerSocket()` que retornam instâncias de `SSLServerSocket`, análogos aos construtores de `java.net.ServerSocket`:

```java
public abstract ServerSocket createServerSocket(int port) throws IOException
public abstract ServerSocket createServerSocket(int port, int queueLength) throws IOException  
public abstract ServerSocket createServerSocket(int port, int queueLength, InetAddress interface) throws IOException
```

Configuração Adicional Necessária

A fábrica retornada por `SSLServerSocketFactory.getDefault()` geralmente só suporta autenticação do servidor. Para habilitar criptografia, soquetes do lado do servidor requerem configuração adicional, que varia conforme a implementação JSSE.

Na implementação de referência da Sun, usamos um objeto `SSLContext` para criar soquetes de servidor totalmente configurados. O processo envolve:

1. Gerar chaves públicas e certificados usando `keytool`
2. Validar certificados com uma autoridade certificadora (como Comodo)
3. Criar um `SSLContext` para o algoritmo desejado
4. Configurar fábricas de gerenciadores de chaves e certificados
5. Carregar e inicializar um armazenamento de chaves (keystore)

Exemplo Prático

O Exemplo 10-2 mostra um `SecureOrderTaker` completo que aceita pedidos seguros:

```java
import java.io.*;
import java.net.*;
import java.security.*;
import javax.net.ssl.*;

public class SecureOrderTaker {
    public final static int PORT = 7000;
    
    public static void main(String[] args) {
        try {
            SSLContext context = SSLContext.getInstance("SSL");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            KeyStore ks = KeyStore.getInstance("JKS");
            
            // Carrega keystore com senha
            char[] password = System.console().readPassword();
            ks.load(new FileInputStream("jnp4e.keys"), password);
            kmf.init(ks, password);
            
            context.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory factory = context.getServerSocketFactory();
            SSLServerSocket server = (SSLServerSocket) factory.createServerSocket(PORT);
            
            // Configura suites de cifra
            String[] supported = server.getSupportedCipherSuites();
            server.setEnabledCipherSuites(supported);
            
            while (true) {
                try (Socket connection = server.accept()) {
                    InputStream in = connection.getInputStream();
                    int c;
                    while ((c = in.read()) != -1) {
                        System.out.write(c);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
```

Gerando Chaves com keytool

Para criar o arquivo de chaves (`jnp4e.keys`), use:

```
keytool -genkey -alias ourstore -keystore jnp4e.keys
```

O processo solicitará informações de identificação e senha. Para uso em produção, você precisará validar o certificado com uma autoridade certificadora.

Alternativas para Testes

Para experimentação, você pode:
1. Usar o keystore de testes da Oracle (`testkeys` com senha "passphrase")
2. Habilitar suites de cifra anônimas (não autenticadas), como:
    - `SSL_DH_anon_WITH_3DES_EDE_CBC_SHA`
    - `TLS_DH_anon_WITH_AES_128_CBC_SHA`

Essas cifras são vulneráveis a ataques "man-in-the-middle", mas permitem testes sem custos com certificados.


###### Configuring SSLServerSockets


Depois de criar e inicializar com sucesso um `SSLServerSocket`, muitos aplicativos podem ser desenvolvidos usando apenas os métodos herdados de `java.net.ServerSocket`. No entanto, há situações em que é necessário ajustar seu comportamento.

Assim como o `SSLSocket`, o `SSLServerSocket` fornece métodos para:
- **Selecionar conjuntos de cifras**
- **Gerenciar sessões**
- **Definir se os clientes devem se autenticar**

A maioria desses métodos tem nomes semelhantes aos equivalentes no `SSLSocket`, mas com uma diferença crucial: eles operam no **lado do servidor** e definem configurações padrão para todos os soquetes aceitos por um `SSLServerSocket`.

+ Ponto Importante

Em alguns casos, após aceitar um `SSLSocket` específico (via `accept()`), ainda é possível usar os métodos do `SSLSocket` para configurar aquele soquete individualmente, em vez de afetar todos os soquetes aceitos pelo `SSLServerSocket`.

+  Exemplo de Métodos de Configuração

```java
// Define os conjuntos de cifras habilitados (padrão para todos os soquetes aceitos)
sslServerSocket.setEnabledCipherSuites(new String[] {"TLS_AES_128_GCM_SHA256"});

// Habilita/desabilita a criação de sessões compartilhadas
sslServerSocket.setEnableSessionCreation(true);

// Exige autenticação do cliente
sslServerSocket.setNeedClientAuth(true);
```  

Essa abordagem permite balancear entre configurações globais (aplicadas a todas as conexões) e personalizações por conexão.


###### Choosing the Cipher Suites

A classe `SSLServerSocket` possui os mesmos três métodos do `SSLSocket` para determinar quais conjuntos de cifras são suportados e habilitados:

```java
public abstract String[] getSupportedCipherSuites()  
public abstract String[] getEnabledCipherSuites()  
public abstract void setEnabledCipherSuites(String[] suites)  
```  

Esses métodos usam os mesmos nomes de conjuntos de cifras que seus equivalentes no `SSLSocket`. A diferença é que **se aplicam a todos os soquetes aceitos pelo `SSLServerSocket`**, não apenas a um `SSLSocket` individual.

**Exemplo: Habilitando Conexões Anônimas**

O trecho abaixo habilita conexões anônimas (não autenticadas) no servidor, identificando conjuntos de cifras que contêm `"_anon_"` em seus nomes (convenção usada na implementação de referência da Oracle):

```java
// Obtém todos os conjuntos de cifras suportados
String[] supported = server.getSupportedCipherSuites();
String[] anonCipherSuitesSupported = new String[supported.length];
int numAnonCipherSuitesSupported = 0;

// Filtra apenas os conjuntos anônimos
for (int i = 0; i < supported.length; i++) {
    if (supported[i].indexOf("_anon_") > 0) {
        anonCipherSuitesSupported[numAnonCipherSuitesSupported++] = supported[i];
    }
}

// Combina os conjuntos já habilitados com os anônimos
String[] oldEnabled = server.getEnabledCipherSuites();
String[] newEnabled = new String[oldEnabled.length + numAnonCipherSuitesSupported];
System.arraycopy(oldEnabled, 0, newEnabled, 0, oldEnabled.length);
System.arraycopy(anonCipherSuitesSupported, 0, newEnabled, oldEnabled.length, numAnonCipherSuitesSupported);

// Aplica a nova configuração
server.setEnabledCipherSuites(newEnabled);
```  

**Funcionamento do Código**

1. **Listagem**: Obtém todos os conjuntos de cifras suportados (`getSupportedCipherSuites()`).
2. **Filtragem**: Identifica os conjuntos anônimos (com `"_anon_"` no nome).
3. **Combinação**: Mescla os conjuntos já habilitados (`getEnabledCipherSuites()`) com os anônimos.
4. **Configuração**: Atualiza os conjuntos habilitados no servidor (`setEnabledCipherSuites()`).

**Observação**

- A convenção de nomes com `"_anon_"` é específica da implementação da Oracle. Outras implementações JSSE podem usar padrões diferentes.
- Conexões anônimas são vulneráveis a ataques *man-in-the-middle* e devem ser usadas apenas em ambientes controlados ou para testes.

Isso permite flexibilidade na configuração do servidor, balanceando entre segurança e compatibilidade conforme necessário.

###### Session Management

Tanto o cliente quanto o servidor precisam concordar para estabelecer uma sessão. No lado do servidor, os métodos `setEnableSessionCreation()` e `getEnableSessionCreation()` controlam se a criação de sessões é permitida:

```java
public abstract void setEnableSessionCreation(boolean allowSessions)
public abstract boolean getEnableSessionCreation()
```

**Comportamento padrão:**
- A criação de sessões está habilitada por padrão (allowSessions = true)

**Cenários possíveis:**
1. Se o servidor desativar a criação de sessões (`setEnableSessionCreation(false)`):
    - Clientes que solicitarem uma sessão ainda poderão se conectar
    - Porém não conseguirão estabelecer uma sessão
    - Será necessário fazer um novo handshake para cada soquete

2. Se o cliente recusar sessões mas o servidor permitir:
    - A comunicação ainda ocorrerá normalmente
    - Porém não utilizará sessões compartilhadas

**Observação importante:**
A criação de sessões é uma otimização de desempenho que reduz a necessidade de handshakes repetidos, mas não afeta a capacidade básica de comunicação entre cliente e servidor quando desativada.


###### Client Mode

A classe `SSLServerSocket` oferece métodos para configurar a autenticação de clientes e definir o modo de operação:

**Configurando Autenticação do Cliente**

Os métodos `setNeedClientAuth()` e `getNeedClientAuth()` controlam se os clientes precisam se autenticar:

```java
public abstract void setNeedClientAuth(boolean flag)  
public abstract boolean getNeedClientAuth()  
```  

- **`setNeedClientAuth(true)`**: Exige autenticação do cliente (apenas conexões com certificados válidos serão aceitas)
- **`setNeedClientAuth(false)`** (padrão): Não exige autenticação
- **`getNeedClientAuth()`**: Verifica o estado atual

**Modo Cliente em um Socket Servidor**

O método `setUseClientMode()` permite que um `SSLServerSocket` se comporte como cliente em negociações SSL (útil para cenários como transferência de arquivos FTP, onde o cliente abre um socket servidor para receber dados):

```java
public abstract void setUseClientMode(boolean flag)  
public abstract boolean getUseClientMode()  
```  

- **`setUseClientMode(true)`**: Trata o `SSLServerSocket` como "cliente" nas negociações SSL (não solicita certificado ao servidor remoto)
- **`getUseClientMode()`**: Retorna `true` se estiver em modo cliente

**Exemplo de Uso**

```java
SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port);  

// Exige autenticação do cliente  
serverSocket.setNeedClientAuth(true);  

// Configura como "cliente" para conexões de dados (ex: FTP)  
serverSocket.setUseClientMode(false);  
```  

Esses métodos permitem flexibilidade em cenários onde:
1. Um servidor precisa validar a identidade dos clientes (ex: aplicações bancárias)
2. Um programa cliente precisa abrir portas para receber dados (ex: FTP passivo)

O modo padrão (`false` em ambos) é adequado para a maioria dos servidores SSL convencionais.
