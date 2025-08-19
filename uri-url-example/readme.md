
### URLs e URIs


 **Conceitos Fundamentais**  
- **URL (Uniform Resource Locator)**:  
  Identifica a **localização** de um recurso na Internet (ex.: `https://www.exemplo.com/pagina.html`).  
  - Contém:  
    - Protocolo (`http`, `ftp`, etc.)  
    - Domínio/IP  
    - Caminho do recurso  

- **URI (Uniform Resource Identifier)**:  
  Termo mais amplo que inclui:  
  - URLs (recursos por localização)  
  - URNs (recursos por nome/número, ex.: `urn:isbn:0451450523`)  

**A Classe `URL` em Java**  

- **Funcionalidade**:  
  - Abstrai a complexidade de protocolos (HTTP, FTP, etc.).  
  - Permite buscar recursos com poucas linhas de código.  

- **Exemplo Básico**:  
  ```java
  URL url = new URL("https://www.exemplo.com/dados.txt");
  InputStream in = url.openStream(); // Lê o conteúdo
  ```

**Por Que Usar URLs?**  
1. **Simplicidade**: Não requer configuração manual de sockets ou protocolos.  
2. **Portabilidade**: Funciona com diversos protocolos (HTTP, HTTPS, FTP, etc.).  
3. **Integração**: Ideal para aplicações web (baixar páginas, imagens, APIs).  

**Diferença Entre URL e URI**  

| **URL**                          | **URI**                           |  
|----------------------------------|-----------------------------------|  
| Identifica recursos por localização | Pode identificar por nome ou localização |  
| Ex.: `http://exemplo.com`        | Ex.: `urn:issn:0167-6423`        |  

**Exemplo Prático**  

```java
import java.net.*;
import java.io.*;

public class LeitorURL {
    public static void main(String[] args) throws IOException {
        URL url = new URL("https://api.github.com/users/openai");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream()))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                System.out.println(linha);
            }
        }
    }
}
```
**Saída**: Exibe o JSON do perfil do usuário "openai" no GitHub.

 **Casos de Uso**  
- **Acessar APIs REST** (como no exemplo acima).  
- **Baixar arquivos** de servidores remotos.  
- **Parser de HTML** para extrair links de páginas web.  

**Limitações**  
- **Protocolos Suportados**: Depende da JVM (HTTP/HTTPS são universais; outros como `ftp` podem variar).  
- **Controle Limitado**: Para operações avançadas (como headers HTTP), use `URLConnection`.  

O capítulo explora detalhes como:  
- Composição de URLs (protocolo, host, porta, caminho).  
- Classe `URI` para manipulação mais segura.  
- `URLConnection` para personalizar requisições.  

Em resumo, a classe `URL` é a porta de entrada para recursos na web em Java, combinando simplicidade e poder. 


#### URIs

Uma **URI (Uniform Resource Identifier)** é uma string que identifica um recurso na Internet ou em outro sistema. Pode ser:  

- **URL (Localizador)**: Indica **onde** o recurso está (ex.: `https://exemplo.com/doc`).  
- **URN (Nomeador)**: Identifica **por nome/número**, independente da localização (ex.: `urn:isbn:0451450523`).  

##### **Estrutura Básica**  

"scheme:esquema-específico"

- **Scheme (Esquema)**: Define o protocolo ou tipo (ex.: `http`, `ftp`, `mailto`).  
- **Parte Específica**: Varia conforme o esquema.  

##### **Componentes de uma URI Hierárquica**  

Muitas URIs seguem o padrão:  
```  
//autoridade/caminho?consulta#fragmento  
```  
- **Autoridade**: Normalmente um host (ex.: `www.exemplo.com`), podendo incluir usuário/porta (ex.: `ftp://user:senha@host:21`).  
- **Caminho**: Hierarquia de recursos (ex.: `/pasta/arquivo.html`).  
- **Consulta (Query)**: Parâmetros (ex.: `?id=123&pagina=2`).  
- **Fragmento**: Seção específica (ex.: `#capitulo3`).  


##### **Exemplos de Esquemas Comuns**  

| Esquema  | Exemplo de Uso                    |     |
| -------- | --------------------------------- | --- |
| `http`   | `http://www.exemplo.com`          |     |
| `ftp`    | `ftp://ftp.exemplo.com/arquivo`   |     |
| `mailto` | `mailto:contato@exemplo.com`      |     |
| `urn`    | `urn:isbn:978-0134685991`         |     |
| `data`   | `data:text/plain;base64,SGVsbG8=` |     |


##### **Codificação de Caracteres**  

- **Caracteres Reservados**: Como "/", "?", "=" devem ser usados conforme o esquema.  
- **Caracteres Especiais**: Não-ASCII (ex.: "á", "木") são codificados como "%" + hex (ex.: "%C3%A1" para "á").  
- **Espaços**: Codificados como "%20" ou "+".  

**Exemplo**:  
```  
http://exemplo.com/arquivo%20com%20espaços.txt  
```  


##### **IRIs vs. URIs**  

- **IRI (Internationalized Resource Identifier)**: Permite caracteres Unicode diretamente (ex.: "http://exemplo.com/木").  
- **URI**: Exige codificação ASCII (ex.: "http://exemplo.com/%E6%9C%A8").  


##### **Boas Práticas**  

1. **Seja generoso com URIs**: Qualquer recurso (páginas, mensagens, filtros) deve ter sua própria URI.  
2. **Human-readable**: Estruture URIs de forma legível (ex.: `/livros/1984` em vez de `/id=123`).  
3. **Evite credenciais na URI**: Incluir senhas (ex.: `ftp://user:senha@host`) é um risco de segurança.  


**Exemplo em Java**  

```java
import java.net.URI;
import java.net.URISyntaxException;

public class ExemploURI {
    public static void main(String[] args) {
        try {
            URI uri = new URI("https://www.exemplo.com:8080/path?query=value#frag");
            System.out.println("Esquema: " + uri.getScheme()); // https
            System.out.println("Host: " + uri.getHost()); // www.exemplo.com
            System.out.println("Path: " + uri.getPath()); // /path
        } catch (URISyntaxException e) {
            System.err.println("URI inválida: " + e.getMessage());
        }
    }
}
```


##### **Por Que Isso Importa?**  
- **Web Semântica**: URIs são a base para identificar recursos únicos.  
- **APIs REST**: Usam URIs para acessar recursos (ex.: `/api/usuarios/1`).  
- **Segurança**: Codificação correta evita ambiguidades e ataques.  

Em resumo, URIs são a espinha dorsal da web, permitindo que recursos sejam identificados de forma única e acessados globalmente. 



#### URLs

Uma **URL (Uniform Resource Locator)** é um tipo específico de **URI** que não apenas identifica um recurso, mas também fornece seu **localizador na rede**, permitindo que clientes o acessem.  
- **Comparação**:  
  - **URI**: Identifica um recurso (ex.: `urn:isbn:0451450523`).  
  - **URL**: Diz **onde** e **como** obtê-lo (ex.: `http://exemplo.com/livro.pdf`).  

###### **Estrutura de uma URL**  
```  
protocolo://usuário:senha@host:porta/caminho?consulta#fragmento  
```  
- **Componentes Principais**:  
  1. **Protocolo (Scheme)**: `http`, `ftp`, `https`, `file`, etc.  
  2. **Autoridade**:  
     - **Host**: Domínio (`www.exemplo.com`) ou IP (`192.168.1.1`).  
     - **Porta** (opcional): Padrão depende do protocolo (ex.: 80 para HTTP).  
     - **Credenciais** (raro): `usuário:senha@`.  
  3. **Caminho (Path)**: Localização do recurso no servidor (ex.: `/pasta/arquivo.html`).  
  4. **Consulta (Query)**: Parâmetros para o servidor (ex.: `?id=10&categoria=livros`).  
  5. **Fragmento**: Seção específica do recurso (ex.: `#capitulo3` em HTML).  

###### **Exemplos**  
| URL                                      | Explicação                                                                 |  
|------------------------------------------|---------------------------------------------------------------------------|  
| `http://exemplo.com:8080/docs/index.html` | Acessa `index.html` via HTTP na porta 8080.                               |  
| `ftp://user:pass@ftp.exemplo.com/arquivo` | Download de `arquivo` via FTP com autenticação.                           |  
| `https://api.com/data?format=json`        | Solicita dados em JSON de uma API.                                        |  
| `file:///C:/Users/arquivo.txt`            | Acessa um arquivo local no Windows.                                       |  

###### **Detalhes Importantes**  
- **Document Root**: Servidores web limitam o acesso a um diretório base (ex.: `/var/www`), ocultando o resto do sistema de arquivos.  
- **Fragmentos**:  
  - Em HTML, referenciam âncoras (ex.: `<h2 id="secao1">` → `#secao1`).  
  - Não são enviados ao servidor — processados apenas no cliente.  
- **Codificação**: Caracteres especiais devem ser escapados (ex.: espaços viram `%20`).  

###### **URLs em Java**  
A classe `java.net.URL` permite:  
- **Acessar recursos** diretamente (ex.: baixar páginas web).  
- **Manipular componentes** (protocolo, host, caminho, etc.).  

**Exemplo**:  
```java
import java.net.URL;

public class ExemploURL {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://www.exemplo.com:443/livros?autor=Rowling#harrypotter");
        System.out.println("Protocolo: " + url.getProtocol()); // https
        System.out.println("Host: " + url.getHost());         // www.exemplo.com
        System.out.println("Path: " + url.getPath());         // /livros
        System.out.println("Fragmento: " + url.getRef());     // harrypotter
    }
}
```

###### **Boas Práticas**  
1. **Evite credenciais na URL**: Prefira autenticação por headers ou tokens.  
2. **Use HTTPS** para dados sensíveis.  
3. **Codifique consultas**: Substitua espaços e caracteres especiais (ex.: `Java I/O` → `Java%20I%2FO`).  


URLs são a base da navegação web, combinando **localização** e **protocolo** para acessar recursos. Em Java, a classe `URL` simplifica o trabalho com esses identificadores, abstraindo complexidades de rede. 🌐


##### URLs **relativas**

São URLs **incompletas** que herdam partes (protocolo, host, caminho) do documento onde estão inseridas.  
- **Exemplo**:  
  - **Documento atual**: `http://www.exemplo.com/pasta/arquivo.html`  
  - **Link relativo**: `<a href="outro.html">` → **URL resolvida**: `http://www.exemplo.com/pasta/outro.html`  

##### **Como São Resolvidas?**  
1. **Baseadas no documento atual**:  
   - Se o link for `"arquivo2.html"`, o navegador substitui o nome do arquivo atual pelo novo.  
   - Exemplo:  
     - Documento: `http://site.com/blog/post1.html`  
     - Link: `"post2.html"` → URL final: `http://site.com/blog/post2.html`  

2. **Baseadas na raiz do servidor (/)**:  
   - Links que começam com `/` são relativos ao **document root** do servidor.  
   - Exemplo:  
     - Documento: `http://site.com/blog/post1.html`  
     - Link: `"/sobre.html"` → URL final: `http://site.com/sobre.html`  

##### **Vantagens das URLs Relativas**  
1. **Portabilidade**:  
   - Permitem mover uma árvore de arquivos para outro domínio ou servidor **sem quebrar links internos**.  
   - Exemplo: Copiar `site.com/blog` para `novosite.com/blog` mantém os links funcionando.  

2. **Multiplos Protocolos**:  
   - A mesma estrutura pode ser acessada via `http` e `ftp` sem alterar os links.  

3. **Economia de Código**:  
   - Evita repetir o domínio completo em cada link.  

**Exemplo Prático**  
```html
<!-- No documento: http://www.ibiblio.org/projetos/artigo.html -->
<a href="detalhes.html">Detalhes</a>          <!-- Resolve para: http://www.ibiblio.org/projetos/detalhes.html -->
<a href="/docs/manual.pdf">Manual</a>         <!-- Resolve para: http://www.ibiblio.org/docs/manual.pdf -->
```

**Em Java**  
A classe `URI` pode resolver URLs relativas:  
```java
URI base = new URI("http://www.exemplo.com/pasta/");
URI relativa = new URI("arquivo.html");
URI absoluta = base.resolve(relativa); // http://www.exemplo.com/pasta/arquivo.html
```

##### **Cuidados**  
- **Links quebrados**: Se o documento base for movido, links relativos podem falhar.  
- **Fragmentos**: URLs relativas não afetam o `#fragmento` (ex.: `"#secao2"` mantém o mesmo documento).  


URLs relativas são essenciais para:  
- **Manutenção simplificada** de sites.  
- **Flexibilidade** ao migrar conteúdo.  
- **Links limpos** e menos redundantes.  

Use-as para estruturar projetos web de forma eficiente! 

#### A classe URL

A classe `java.net.URL` é uma representação abstrata de um **Uniform Resource Locator** (URL), como `http://www.exemplo.com` ou `ftp://ftp.arquivos.org/`.  

###### **Características Principais**  
1. **Imutável e Thread-Safe**:  
   - Uma vez criado, um objeto `URL` não pode ser alterado (seus campos são finais).  
   - Seguro para uso em ambientes multithread.  

2. **Estrutura Baseada em Componentes**:  
   - Divide a URL em partes:  
     - **Protocolo** (ex.: `http`, `ftp`).  
     - **Host** (ex.: `www.exemplo.com`).  
     - **Porta** (opcional, padrão depende do protocolo).  
     - **Caminho** (ex.: `/pasta/arquivo.html`).  
     - **Consulta** (ex.: `?id=123`).  
     - **Fragmento** (ex.: `#secao2`).  

3. **Padrão de Projeto (Strategy)**:  
   - Usa **protocol handlers** (manipuladores de protocolo) para tratar diferentes esquemas (HTTP, FTP, etc.).  
   - Permite extensão sem herança (classe `final`).  

###### **Como Criar um Objeto `URL`**  

```java
try {
    URL url = new URL("https://www.exemplo.com:8080/livros?autor=Rowling#harrypotter");
    System.out.println("Protocolo: " + url.getProtocol()); // https
    System.out.println("Host: " + url.getHost());         // www.exemplo.com
    System.out.println("Porta: " + url.getPort());        // 8080
    System.out.println("Path: " + url.getPath());         // /livros
    System.out.println("Fragmento: " + url.getRef());     // harrypotter
} catch (MalformedURLException e) {
    System.err.println("URL inválida!");
}
```

###### **Casos de Uso**  

- **Acessar Recursos na Web**: Baixar páginas, arquivos via HTTP/FTP.  
- **Manipulação Segura**: Evita erros com URLs malformadas.  
- **Extensibilidade**: Novos protocolos podem ser adicionados via `URLStreamHandler`.  

###### **Limitações**  
- **Imutabilidade**: Requer criar um novo objeto para URLs diferentes.  
- **Protocolos Suportados**: Depende da JVM (HTTP/HTTPS são universais; outros como `file` ou customizados podem precisar de configuração).  

**Exemplo Prático (Download Simples)**  
```java
try {
    URL site = new URL("http://www.exemplo.com/arquivo.txt");
    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(site.openStream()))) {
        String linha;
        while ((linha = reader.readLine()) != null) {
            System.out.println(linha);
        }
    }
} catch (IOException e) {
    e.printStackTrace();
}
```

###### **Por Que Usar a Classe `URL`?**  

- **Abstração Simplificada**: Esconde complexidades de sockets e protocolos.  
- **Segurança**: Valida a sintaxe da URL na construção.  
- **Portabilidade**: Funciona em qualquer ambiente Java com suporte aos protocolos.  

Em resumo, `java.net.URL` é a ferramenta essencial para trabalhar com recursos web em Java, combinando imutabilidade, segurança e flexibilidade. 


**1. Construção a Partir de uma String**  

**Método**:  
```java
public URL(String url) throws MalformedURLException
```  
**Exemplo**:  
```java
try {
    URL u = new URL("https://www.exemplo.com/pagina.html");
} catch (MalformedURLException e) {
    System.err.println("URL inválida: " + e.getMessage());
}
```  
**Uso**:  
- Valida se a string é uma URL bem formada (lança `MalformedURLException` se não for).  
- Suporta protocolos como `http`, `https`, `ftp`, `file`, entre outros.  


**Teste de Protocolos Suportados**:  

O programa `ProtocolTester` (Exemplo 5-1) verifica quais protocolos a JVM reconhece.  
**Resultados típicos (Java 7+)**:
```
http is supported
https is supported
ftp is supported
mailto is supported
file is supported
... 
jdbc/rmi/doc não são suportados diretamente pela classe URL.
```


**2. Construção por Partes**  

**Método**:  
```java
public URL(String protocol, String host, String file) throws MalformedURLException
```  
**Exemplo**:  
```java
URL u = new URL("http", "www.exemplo.com", "/recursos/arquivo.html");
```  
- Define o protocolo, host e caminho (a porta usa a **padrão do protocolo**, ex.: 80 para HTTP).  
- **Erro comum**: Esquecer a `/` inicial no caminho (`/arquivo.html` em vez de `arquivo.html`).  

**Com porta explícita**:  
```java
public URL(String protocol, String host, int port, String file) throws MalformedURLException
```  
**Exemplo**:  
```java
URL u = new URL("http", "servidor.com", 8080, "/api/dados");
```


**3. URLs Relativas**  
**Método**:  
```java
public URL(URL base, String relative) throws MalformedURLException
```  
**Exemplo**:  
```java
URL base = new URL("http://www.exemplo.com/docs/");
URL relativa = new URL(base, "manual.html"); // Resulta em http://www.exemplo.com/docs/manual.html
```  
**Uso**:  
- Ideal para resolver links em documentos HTML ou mover conjuntos de arquivos sem quebrar links.  


**4. Outras Fontes de Objetos `URL`**  
- **Applets**:  
  ```java
  URL docBase = getDocumentBase(); // URL da página que contém o applet
  URL codeBase = getCodeBase();    // URL do arquivo .class do applet
  ```  
- **Arquivos Locais**:  
  ```java
  File arquivo = new File("/caminho/arquivo.txt");
  URL url = arquivo.toURI().toURL(); // Converte para URL (ex.: file:/caminho/arquivo.txt)
  ```  
- **Recursos em Classpath**:  
  ```java
  URL recurso = ClassLoader.getSystemResource("imagens/logo.png"); // Busca no classpath
  ```  



 **Cuidados e Observações**  
- **Imutabilidade**: Objetos `URL` são imutáveis (thread-safe).  
- **Protocolos Customizados**: JDBC (`jdbc:mysql://...`) e RMI (`rmi://...`) não são suportados diretamente pela classe `URL` (usam pacotes específicos como `java.sql`).  
- **Plataforma Dependente**: URLs do tipo `file:` variam entre Windows (`file:/D:/...`) e Unix (`file:/home/...`).  


 **Exemplo Completo**  
```java
import java.net.*;
import java.io.*;

public class ExemploURL {
    public static void main(String[] args) {
        try {
            // Construção por partes
            URL absoluta = new URL("https", "api.github.com", "/users/openai");
            System.out.println("URL Absoluta: " + absoluta);

            // URL relativa
            URL base = new URL("http://www.exemplo.com/projetos/");
            URL relativa = new URL(base, "detalhes.html");
            System.out.println("URL Relativa Resolvida: " + relativa);

            // Recuperar recurso do classpath
            URL recurso = ExemploURL.class.getResource("/config.xml");
            System.out.println("Recurso: " + recurso);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
```


A classe `URL` oferece múltiplas formas de construir e manipular URLs, sendo essencial para:  
- **Acessar recursos web** (HTTP, FTP).  
- **Trabalhar com caminhos relativos**.  
- **Integrar recursos locais e remotos**.  

Use-a para simplificar o gerenciamento de endereços em aplicações Java! 

##### Recuperando Dados de uma URL

A classe `java.net.URL` oferece vários métodos para recuperar dados de recursos na web. Aqui estão as principais abordagens:


**1. `openStream()`: Leitura Simples**  
 
**Método**:  
```java
public InputStream openStream() throws IOException
```  
**Funcionamento**:  
- Abre uma conexão com o recurso e retorna um `InputStream` para ler os dados brutos (HTML, imagens, texto, etc.).  
- **Não inclui metadados** (como cabeçalhos HTTP).  

**Exemplo (leitura de texto)**:  
```java
try {
    URL url = new URL("http://exemplo.com/arquivo.txt");
    try (InputStream in = url.openStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
        String linha;
        while ((linha = reader.readLine()) != null) {
            System.out.println(linha);
        }
    }
} catch (IOException e) {
    e.printStackTrace();
}
```  
**Cuidados**:  
- Sempre feche o `InputStream` (use `try-with-resources`).  
- Assume que o recurso é texto (pode falhar com binários como imagens).  


 **2. `openConnection()`: Controle Avançado**  
 
**Método**:  
```java
public URLConnection openConnection() throws IOException
```  
**Vantagens**:  
- Permite configurar cabeçalhos, timeouts, e métodos de requisição (GET/POST).  
- Acessa metadados (como `Content-Type`).  

**Exemplo**:  
```java
URLConnection conn = url.openConnection();
conn.setRequestProperty("User-Agent", "Java/1.8");
try (InputStream in = conn.getInputStream()) {
    // Processar dados...
}
```  
**Uso típico**:  
- Leitura de cabeçalhos HTTP.  
- Envio de dados via POST (formulários).  


 **3. `getContent()`: Objetos Especializados**  
 
**Método**:  
```java
public Object getContent() throws IOException
```  
**Retorno**:  
- Depende do tipo de recurso:  
  - Texto/HTML → `InputStream`.  
  - Imagem (GIF/JPEG) → `ImageProducer`.  
  - Áudio → `AppletAudioClip`.  

**Exemplo**:  
```java
Object conteudo = url.getContent();
if (conteudo instanceof InputStream) {
    // Tratar texto...
} else if (conteudo instanceof ImageProducer) {
    // Carregar imagem...
}
```  
**Limitação**:  
- Difícil prever o tipo de retorno (requer verificações com `instanceof`).  

**Versão com Tipos Preferenciais**:  
```java
Class<?>[] tipos = {String.class, Reader.class, InputStream.class};
Object conteudo = url.getContent(tipos); // Retorna o primeiro tipo suportado
```  


**Comparação entre Métodos**  

| Método               | Uso Recomendado                          | Complexidade |  
|----------------------|------------------------------------------|--------------|  
| `openStream()`       | Leitura rápida de dados brutos.          | Baixa        |  
| `openConnection()`   | Configuração avançada (cabeçalhos, POST).| Alta         |  
| `getContent()`       | Obter objetos especializados (imagens/áudio). | Média    |  

---

 **Exemplo Completo (`SourceViewer`)**  
 
```java
import java.io.*;
import java.net.*;

public class SourceViewer {
    public static void main(String[] args) {
        try {
            URL url = new URL("http://exemplo.com");
            try (InputStream in = url.openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    System.out.println(linha);
                }
            }
        } catch (MalformedURLException e) {
            System.err.println("URL inválida!");
        } catch (IOException e) {
            System.err.println("Erro de leitura: " + e.getMessage());
        }
    }
}
```  
**Saída**: Exibe o conteúdo HTML/bruto do URL.  


**Problemas Comuns**  
1. **Codificação de Caracteres**:  
   - Arquivos não-ASCII podem ter codificação incorreta (use `InputStreamReader` com `Charset`).  
   ```java
   Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
   ```  
2. **Tipos de Conteúdo**:  
   - Verifique `Content-Type` (via `URLConnection.getContentType()`) para tratar binários/texto adequadamente.  

3. **Fechamento de Recursos**:  
   - Sempre use `try-with-resources` ou feche manualmente em `finally`.  


- **Para leitura simples**: `openStream()`.  
- **Para controle total**: `openConnection()`.  
- **Para tipos específicos (imagens/áudio)**: `getContent()`.  

Esses métodos formam a base para acesso a recursos web em Java, desde páginas HTML até downloads de arquivos. 



##### Dividindo uma URL em partes

A classe `java.net.URL` permite acessar os componentes individuais de uma URL através de métodos específicos. Uma URL é dividida em:

###### **Componentes Principais**  
1. **Esquema (Protocolo)**:  
   - `getProtocol()` → Retorna o protocolo (ex.: `"http"`, `"ftp"`).  
   ```java
   URL u = new URL("https://exemplo.com");
   System.out.println(u.getProtocol()); // "https"
   ```

2. **Autoridade**:  
   - `getAuthority()` → Retorna a parte da autoridade (host + porta + usuário, se existir).  
   - `getHost()` → Retorna o domínio (ex.: `"exemplo.com"`).  
   - `getPort()` → Retorna a porta (`-1` se não especificada).  
   - `getUserInfo()` → Retorna credenciais (ex.: `"usuário:senha"`).  

   **Exemplo**:  
   ```java
   URL u = new URL("ftp://admin:12345@ftp.exemplo.com:21/");
   System.out.println(u.getAuthority()); // "admin:12345@ftp.exemplo.com:21"
   System.out.println(u.getHost());      // "ftp.exemplo.com"
   System.out.println(u.getPort());      // 21
   System.out.println(u.getUserInfo());  // "admin:12345"
   ```

3. **Caminho (Path)**:  
   - `getPath()` → Retorna o caminho sem a query string (ex.: `"/pasta/arquivo.html"`).  
   - `getFile()` → Retorna o caminho **incluindo** a query string (ex.: `"/pasta/arquivo.html?param=valor"`).  

4. **Query String**:  
   - `getQuery()` → Retorna os parâmetros após `?` (ex.: `"categoria=livros"`).  

5. **Fragmento (Âncora)**:  
   - `getRef()` → Retorna o identificador após `#` (ex.: `"secao2"`).  


 **Exemplo Completo (`URLSplitter`)**  
 
O programa abaixo decompõe uma URL passada como argumento:  
```java
import java.net.*;

public class URLSplitter {
    public static void main(String[] args) {
        try {
            URL u = new URL("http://admin@exemplo.com:8080/path?query=valor#frag");
            System.out.println("Protocolo: " + u.getProtocol());
            System.out.println("Host: " + u.getHost());
            System.out.println("Porta: " + u.getPort());
            System.out.println("Path: " + u.getPath());
            System.out.println("Query: " + u.getQuery());
            System.out.println("Fragmento: " + u.getRef());
            System.out.println("Autoridade: " + u.getAuthority());
        } catch (MalformedURLException e) {
            System.err.println("URL inválida!");
        }
    }
}
```

**Saída**:  
```
Protocolo: http
Host: exemplo.com
Porta: 8080
Path: /path
Query: query=valor
Fragmento: frag
Autoridade: admin@exemplo.com:8080
```


###### **Casos Especiais**  
- **URLs sem porta**: `getPort()` retorna `-1`.  
- **URLs sem query/fragmento**: `getQuery()` e `getRef()` retornam `null`.  
- **URLs `mailto`**: O caminho contém o destinatário (ex.: `mailto:user@exemplo.com`).  

###### **Por Que Isso é Útil?**  
- **Manipulação de URLs**: Construir ou modificar URLs dinamicamente.  
- **Análise de requisições**: Extrair parâmetros de queries em aplicações web.  
- **Segurança**: Validar partes da URL (como credenciais embutidas).  

Use esses métodos para trabalhar com URLs de forma precisa e segura! 🔍

##### Igualdade e Comparação

Métodos `equals()` e `hashCode()`

- **`equals()`**:  
  - Compara duas URLs para verificar se apontam para o **mesmo recurso**.  
  - **Critérios**:  
    - Mesmo protocolo, host, porta, caminho, query string e fragmento.  
    - **Faz consulta DNS** para resolver hosts (ex.: `www.ibiblio.org` vs. `ibiblio.org` são considerados iguais).  
    - **Bloqueante**: Pode ser lento devido à operação de rede.  
  - **Exemplo**:  
    ```java
    URL url1 = new URL("http://www.exemplo.com/");
    URL url2 = new URL("http://exemplo.com/");
    System.out.println(url1.equals(url2)); // Pode retornar true após consulta DNS.
    ```

- **`hashCode()`**:  
  - Baseado nos mesmos critérios de `equals()`.  
  - **Cuidado**: Evite usar URLs como chaves em `HashMap` (consulta DNS pode impactar performance).  

###### **Método `sameFile()`**
- **Comparação sem fragmento**:  
  - Verifica se duas URLs apontam para o **mesmo arquivo**, ignorando o fragmento (parte após `#`).  
  - **Exemplo**:  
    ```java
    URL u1 = new URL("http://exemplo.com/doc.html#secao1");
    URL u2 = new URL("http://exemplo.com/doc.html#secao2");
    System.out.println(u1.sameFile(u2)); // true (mesmo arquivo, fragmentos diferentes).
    ```

###### **Conversão para String e URI**
- **`toString()` e `toExternalForm()`**:  
  - Retornam a URL como string absoluta (ex.: `"http://exemplo.com/path"`).  
  - Útil para exibição ou uso em navegadores.  

- **`toURI()`**:  
  - Converte a URL para um objeto `URI` (mais robusto para manipulação).  
  - **Vantagens**:  
    - **Não bloqueante** (não faz DNS em `equals()`).  
    - Melhor suporte para codificação e absolutização de caminhos.  
  - **Exemplo**:  
    ```java
    URI uri = url.toURI(); // Converte URL para URI.
    ```

###### **Recomendações**
1. **Evite `equals()` em coleções**:  
   - Prefira `URI` para armazenar em `HashMap` ou `HashSet`.  
   ```java
   URI uri1 = new URI("http://exemplo.com");
   URI uri2 = new URI("http://exemplo.com");
   System.out.println(uri1.equals(uri2)); // Rápido (sem DNS).
   ```

2. **Use `sameFile()` para comparar recursos**:  
   - Ideal para verificar se URLs referenciam o mesmo arquivo, mesmo com fragmentos diferentes.  

3. **Converta para URI quando possível**:  
   - Operações como codificação/decodificação são mais precisas com `URI`.  

###### **Exemplo Prático**
```java
import java.net.*;

public class URLComparison {
    public static void main(String[] args) throws Exception {
        URL url1 = new URL("https://exemplo.com/path?query=1#top");
        URL url2 = new URL("https://exemplo.com/path?query=1#bottom");

        System.out.println("Equals: " + url1.equals(url2)); // false (fragmentos diferentes).
        System.out.println("SameFile: " + url1.sameFile(url2)); // true (ignora fragmento).

        // Convertendo para URI
        URI uri = url1.toURI();
        System.out.println("URI: " + uri); // "https://exemplo.com/path?query=1#top"
    }
}
```


- **`equals()`**: Comparação detalhada (com DNS), mas pode ser lenta.  
- **`sameFile()`**: Foca no recurso, ignorando fragmentos.  
- **`URI`**: Melhor opção para armazenamento e manipulação segura.  

Use `URL` para downloads e `URI` para operações estruturais!

#### A classe URI

Uma **URI** (Uniform Resource Identifier) é uma generalização de uma **URL**, incluindo não apenas **Uniform Resource Locators** (URLs), mas também **Uniform Resource Names** (URNs). Embora a maioria das URIs usadas na prática sejam URLs, especificações como XML são definidas em termos de URIs.  

Em Java, as URIs são representadas pela classe **`java.net.URI`**, que difere da **`java.net.URL`** em três aspectos principais:  
1. **Identificação e análise**: A classe `URI` é focada na identificação e análise de URIs, **sem métodos para recuperar recursos**.  
2. **Conformidade**: A classe `URI` segue as especificações mais rigorosamente que a classe `URL`.  
3. **URIs relativas**: A classe `URI` pode representar URIs relativas, enquanto a classe `URL` converte todas em absolutas.  

###### **URI vs. URL**  
- **`URL`**: Usada para recuperação de recursos via protocolos de rede (como HTTP).  
- **`URI`**: Usada para manipulação de strings (ex.: namespaces em XML).  
- **Conversão**:  
  - De `URI` para `URL`: `uri.toURL()`  
  - De `URL` para `URI`: `url.toURI()`  

###### **Construindo uma URI**  

A classe `URI` permite criar objetos a partir de strings completas ou partes individuais, com vários construtores:  
1. **URI completa**:  
   ```java  
   URI voz = new URI("tel:+1-800-9988-9938");  
   ```  
   (Lança `URISyntaxException` se a sintaxe for inválida.)  

2. **Partes específicas**:  
   ```java  
   URI absolute = new URI("http", "//www.ibiblio.org", null);  
   URI relative = new URI(null, "/javafaq/index.shtml", "today");  
   ```  

3. **URIs hierárquicas** (como HTTP/FTP):  
   ```java  
   URI today = new URI("http", "www.ibiblio.org", "/javafaq/index.html", "today");  
   ```  

4. **Com query string**:  
   ```java  
   URI today = new URI("http", "www.ibiblio.org", "/javafaq/index.html", "referrer=cnet", "today");  
   ```  

5. **Construtor detalhado** (com usuário, porta, etc.):  
   ```java  
   URI ftp = new URI("ftp", "anonymous:elharo@ibiblio.org", "ftp.oreilly.com", 21, "/pub/stylesheet", null, null);  
   ```  

###### **Método Estático `URI.create()`**  
Para URIs conhecidamente válidas, evite exceções checadas com:  
```java  
URI styles = URI.create("ftp://anonymous:elharo@ibiblio.org@ftp.oreilly.com:21/pub/stylesheet");  
```  
(Se inválida, lança `IllegalArgumentException`.)  


Use `URI` para manipulação segura de identificadores e `URL` para acesso a recursos. A classe `URI` é mais flexível e adere melhor aos padrões, enquanto `URL` é mais voltada para operações de rede.


##### As partes do URI.


Uma **URI** (Uniform Resource Identifier) é composta por até três partes principais:  
1. **Esquema (Scheme)**: Indica o protocolo (ex: `http`, `ftp`, `tel`).  
2. **Parte Específica do Esquema (Scheme-Specific Part)**: Contém os detalhes dependentes do esquema.  
3. **Identificador de Fragmento (Fragment)**: Referência interna (ex: `#seção` em URLs).  

**Formato Geral**:  
```
scheme:scheme-specific-part:fragment
```  

- Se o **esquema** for omitido, a URI é **relativa**.  
- Se o **fragmento** for omitido, a URI é considerada "pura".  

###### **Métodos para Extrair Partes da URI (Classe `java.net.URI`)**  

A classe `URI` oferece métodos para acessar as partes codificadas (**raw**) e decodificadas:  

###### **Métodos Básicos**  
- `getScheme()` → Retorna o esquema (ex: `"http"`).  
- `getSchemeSpecificPart()` → Retorna a parte específica **decodificada**.  
- `getRawSchemeSpecificPart()` → Retorna a parte específica **codificada**.  
- `getFragment()` → Retorna o fragmento **decodificado**.  
- `getRawFragment()` → Retorna o fragmento **codificado**.  

**Observação**:  
- Não existe `getRawScheme()` porque nomes de esquema **sempre usam caracteres ASCII válidos** (sem codificação).  
- Se um componente não existir (ex: URI sem fragmento), retorna `null`.  


###### **URIs Absolutas vs. Relativas**  
- **URI absoluta**: Possui um esquema (ex: `http://exemplo.com`).  
- **URI relativa**: Não tem esquema (ex: `/caminho/arquivo`).  

**Método**:  
- `isAbsolute()` → Retorna `true` se a URI for absoluta.  

###### **URIs Hierárquicas vs. Opacas**  

- **Hierárquicas**: Seguem uma estrutura de autoridade/caminho (ex: URLs `http`, `ftp`).  
- **Opacas**: Não seguem estrutura hierárquica (ex: URIs `tel`, `urn`).  

**Método**:  
- `isOpaque()` → Retorna `true` se a URI **não for hierárquica**.  


###### **Métodos para URIs Hierárquicas**  

Se a URI for hierárquica, é possível extrair:  
- `getAuthority()` → Autoridade (ex: `"user@host:porta"`).  
- `getHost()` → Nome do host.  
- `getPort()` → Porta (retorna `-1` se não especificada).  
- `getPath()` → Caminho.  
- `getQuery()` → String de consulta (ex: `?param=valor`).  
- `getUserInfo()` → Informações de usuário (ex: `"user:senha"`).  

**Versões "Raw" (Codificadas)**:  
- `getRawAuthority()`, `getRawPath()`, etc. → Retornam os componentes **sem decodificação**.  


###### **Tratamento de Autoridade**  

Se a URI tiver uma autoridade malformada, o método:  
```java  
parseServerAuthority()  
```  
força uma nova análise, extraindo `userInfo`, `host` e `port` separadamente.  
- Se falhar, lança `URISyntaxException`.  


###### **Exemplo Prático**  
O programa abaixo (`URISplitter`) analisa URIs passadas como argumento, mostrando suas partes:  
```java  
import java.net.*;  

public class URISplitter {  
    public static void main(String[] args) {  
        for (String arg : args) {  
            try {  
                URI u = new URI(arg);  
                System.out.println("URI: " + u);  

                if (u.isOpaque()) {  
                    System.out.println("URI Opaca");  
                    System.out.println("Esquema: " + u.getScheme());  
                    System.out.println("Parte Específica: " + u.getSchemeSpecificPart());  
                } else {  
                    System.out.println("URI Hierárquica");  
                    System.out.println("Host: " + u.getHost());  
                    System.out.println("Caminho: " + u.getPath());  
                }  
            } catch (URISyntaxException e) {  
                System.err.println(arg + " não é uma URI válida.");  
            }  
        }  
    }  
}  
```  

**Saída de Exemplo**:  
```  
> java URISplitter tel:+1-800-9988-9938 http://exemplo.com/path#fragment  

URI: tel:+1-800-9988-9938  
URI Opaca  
Esquema: tel  
Parte Específica: +1-800-9988-9938  

URI: http://exemplo.com/path#fragment  
URI Hierárquica  
Host: exemplo.com  
Caminho: /path  
Fragmento: fragment  
```  


- Use `getScheme()`, `getPath()`, etc., para extrair partes **decodificadas**.  
- Use `getRawFoo()` para acessar partes **codificadas**.  
- URIs **opacas** só têm esquema, parte específica e fragmento.  
- URIs **hierárquicas** permitem acesso a host, porta, caminho, etc.




##### Resolução de URIs Relativos:

A classe `URI` possui três métodos para conversão entre URIs relativos e absolutos:
- `public URI resolve(URI uri)`
- `public URI resolve(String uri)`
- `public URI relativize(URI uri)`

Os métodos `resolve()` comparam o argumento `uri` com o URI atual e constroem um novo objeto `URI` absoluto. Por exemplo:
```java
URI absolute = new URI("http://www.example.com/");
URI relative = new URI("images/logo.png");
URI resolved = absolute.resolve(relative); // Resultado: http://www.example.com/images/logo.png
```

Se o URI base não for absoluto, o `resolve()` retorna um URI relativo. O método `relativize()` faz o inverso, convertendo um URI absoluto em relativo:
```java
URI absolute = new URI("http://www.example.com/images/logo.png");
URI top = new URI("http://www.example.com/");
URI relative = top.relativize(absolute); // Resultado: images/logo.png
```

##### Igualdade e Comparação

URIs são comparados considerando:
- **Esquema e autoridade**: Case-insensitive (ex: `http` é igual a `HTTP`).
- **Restante do URI**: Case-sensitive, exceto escapes hexadecimais.
- **Hierárquico vs. Opaco**: URIs hierárquicos são considerados "menores" que opacos.
- **Ordem de comparação**: Esquema → parte opaca → fragmento → autoridade → caminho → query → fragmento.

##### Representação em String

- `toString()`: Retorna o URI sem codificação (útil para exibição humana).
- `toASCIIString()`: Retorna o URI codificado (garante sintaxe correta).

##### Codificação x-www-form-urlencoded

Caracteres em URLs devem ser limitados a um subconjunto ASCII seguro. Caracteres especiais são codificados como `%` seguido de dois dígitos hexadecimais. Espaços podem ser codificados como `+`.

**Exemplo de codificação**:
```java
String encoded = URLEncoder.encode("This string has spaces", "UTF-8"); // "This+string+has+spaces"
```

**Atenção**: Codifique cada parte do URL separadamente para evitar codificação excessiva de caracteres reservados (como "/", "?", "=").

**Decodificação**:
```java
String decoded = URLDecoder.decode("This+string+has+spaces", "UTF-8"); // "This string has spaces"
```

##### Classe `QueryString`

Exemplo de uso para construir queries codificadas:
```java
QueryString qs = new QueryString();
qs.add("hl", "en");
qs.add("q", "Java");
String url = "http://example.com/search?" + qs; // "http://example.com/search?hl=en&q=Java"
```

##### Pontos-chave:
- Use `UTF-8` para codificação/decodificação.
- Codifique partes individuais da URL, não a URL completa.
- `URLEncoder` e `URLDecoder` tratam de conversões entre caracteres especiais e sequências percent-encoded.


#### Proxies

A classe `LocalProxySelector` é uma implementação personalizada de `ProxySelector` que decide se uma conexão deve usar um **proxy** ou não com base em regras específicas.  

##### **Funcionamento**:  
1. **Método `select(URI uri)`**:  
   - Verifica se a URI está em uma lista de falhas (`failed`) ou se o esquema não é **HTTP**.  
     - Se sim, retorna `Proxy.NO_PROXY` (sem proxy).  
     - Se não, configura um proxy HTTP em `proxy.example.com:8000`.  

   ```java
   SocketAddress proxyAddress = new InetSocketAddress("proxy.example.com", 8000);
   Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
   result.add(proxy);
   ```

2. **Método `connectFailed(URI uri, SocketAddress address, IOException ex)`**:  
   - Se uma conexão falhar, a URI é adicionada à lista `failed` para evitar tentativas futuras.  

#####  **Configuração do `ProxySelector` Padrão**  

- Cada JVM tem um único `ProxySelector` global.  
- Para definir um novo seletor:  
  ```java
  ProxySelector selector = new LocalProxySelector();
  ProxySelector.setDefault(selector);
  ```
- **Atenção**: Alterar o `ProxySelector` padrão afeta **todas as conexões** da JVM.  
  - Evite fazer isso em ambientes compartilhados (ex: servlets), pois impactaria outras aplicações no mesmo container.  

##### **Casos de Uso**:  

- Útil para direcionar tráfego HTTP através de um proxy, com fallback para conexão direta em caso de falhas.  
- Permite controle dinâmico de proxies com base em histórico de erros.  

**Exemplo Simplificado**:  
```java
ProxySelector.setDefault(new LocalProxySelector());  
// Todas as conexões HTTP seguirão as regras do LocalProxySelector
```

##### **Principais Pontos**:  
- **Seletor Global**: Afeta toda a JVM.  
- **Fallback Automático**: Conexões com falha são desviadas para `NO_PROXY`.  
- **Cuidado em Ambientes Compartilhados**: Modificações podem ter efeitos colaterais.


#### Comunicação com programas do lado do servidor por meio de GET


A classe `URL` em Java permite que aplicações e *applets* se comuniquem facilmente com programas servidores que usam o método **GET**, como:  
- **CGIs**  
- **Servlets**  
- **Páginas PHP**  
- Outros sistemas que processam requisições HTTP GET.  

*(Para programas que usam **POST**, é necessário usar `URLConnection`, discutido posteriormente.)*  


##### **Como Funciona?**  

1. **Construção da URL com Parâmetros**  
   - Os parâmetros devem ser enviados como uma **string de consulta** (*query string*).  
   - Nomes e valores devem ser codificados usando `URLEncoder.encode()` (formato `x-www-form-urlencoded`).  

2. **Obtendo os Parâmetros Necessários**  
   - Se você desenvolveu o programa servidor, já sabe quais parâmetros ele espera.  
   - Se estiver usando uma API ou software de terceiros, consulte a documentação.  
   - Se o programa processa um **formulário HTML**, os parâmetros são definidos pelos atributos:  
     - `METHOD="GET"` (indica que usa GET).  
     - `ACTION="URL"` (define o endpoint).  
     - `NAME="..."` (define os nomes dos campos).  

---

##### **Exemplo Prático: Formulário de Busca**  

Considere este formulário HTML para uma busca no Google:  
```html
<form action="http://www.google.com/search" method="get">
  <input name="q" /> <!-- Campo de busca -->
  <input type="hidden" name="domains" value="cafeconleche.org" />
  <input type="submit" value="Buscar" />
</form>
```  
- **Parâmetros esperados**: `q` (termo de busca) e `domains` (valor fixo).  
- **URL gerada**:  
  ```
  http://www.google.com/search?q=java&domains=cafeconleche.org
  ```

---

##### **Implementação em Java**  

1. **Codificar os parâmetros** (usando `URLEncoder`):  
   ```java
   String query = "q=" + URLEncoder.encode("java", "UTF-8") 
                + "&domains=" + URLEncoder.encode("cafeconleche.org", "UTF-8");
   ```

2. **Criar a URL e ler a resposta**:  
   ```java
   URL url = new URL("http://www.google.com/search?" + query);
   try (InputStream in = url.openStream()) {
       // Ler e processar a resposta...
   }
   ```

---

##### **Exemplo Completo: Busca no Open Directory**  

O código abaixo realiza uma busca no Open Directory (dmoz.org):  
```java
import java.io.*;
import java.net.*;

public class DMoz {
    public static void main(String[] args) {
        String query = URLEncoder.encode(String.join(" ", args), "UTF-8");
        String urlStr = "http://www.dmoz.org/search?q=" + query;

        try (InputStream in = new URL(urlStr).openStream()) {
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
**Como usar**:  
``` java 
DMoz "termo de busca"
```

---

##### **Pontos Importantes**  
✅ **GET vs. POST**:  
   - **GET** → Parâmetros na URL (limitado em tamanho).  
   - **POST** → Dados enviados no corpo da requisição (mais seguro para dados sensíveis).  

✅ **Codificação obrigatória**:  
   - Sempre use `URLEncoder` para evitar caracteres inválidos na URL.  

⚠ **Cuidado com entradas inesperadas**:  
   - Programas servidores devem validar entradas para evitar ataques (ex: SQL Injection).  


Com a classe `URL` e `URLEncoder`, é fácil enviar requisições GET a servidores e processar respostas. Esse método é útil para integração com APIs, *web scraping* e automação de interações com páginas web.


#### A classe Authenticator


Alguns sites exigem **autenticação** (usuário e senha) para acesso. Existem dois métodos principais:  
1. **Autenticação HTTP** (padrão, suportada pelo Java).  
2. **Autenticação via cookies e formulários HTML** (mais complexa, varia por site).  


#####  **1. Autenticação HTTP com a Classe `Authenticator`**  

Java fornece a classe abstrata `java.net.Authenticator` para lidar com autenticação HTTP.  


1. **Crie uma subclasse** de `Authenticator` e sobrescreva o método:  
   ```java
   protected PasswordAuthentication getPasswordAuthentication()
   ```  
   - Ele deve retornar um objeto `PasswordAuthentication` com **usuário** (`String`) e **senha** (`char[]`).  

2. **Configure o `Authenticator` padrão**:  
   ```java
   Authenticator.setDefault(new MeuAutenticador());
   ```  

3. **Quando uma URL protegida é acessada**, o Java chama automaticamente `getPasswordAuthentication()` para obter as credenciais.  

#####  **Exemplo Simplificado**  
```java
public class MeuAutenticador extends Authenticator {
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String user = "admin";
        char[] pass = {'s', 'e', 'n', 'h', 'a'};
        return new PasswordAuthentication(user, pass);
    }
}
```  

---

#####  **2. Coletando Credenciais via Interface Gráfica (Swing)**  
Para interação com o usuário, use `JPasswordField` (Swing) para capturar a senha de forma segura.  

#####  **Exemplo com `JPasswordField`**  
```java
JTextField userField = new JTextField(20);
JPasswordField passField = new JPasswordField(20);
// ...
char[] senha = passField.getPassword(); // Senha em char[] (mais seguro que String)
```  

#####  **Dialogo de Autenticação Completo**  
A classe `DialogAuthenticator` (Exemplo 5-11) mostra:  
- Um diálogo modal com campos para **usuário** e **senha**.  
- Botões **OK** (envia credenciais) e **Cancelar** (aborta).  
- A senha é armazenada como `char[]` para segurança.  

---

#####  **3. Acessando URLs Protegidas**  

O Exemplo 5-12 (`SecureSourceViewer`) demonstra como baixar páginas protegidas:  
1. Define `DialogAuthenticator` como autenticador padrão.  
2. Ao acessar uma URL, o Java solicita credenciais via diálogo.  
3. Se as credenciais forem válidas, o conteúdo é exibido.  

**Uso**:  
```java

java SecureSourceViewer https://site-protegido.com
```  


#####  **Pontos Importantes**  
✅ **Segurança**:  
   - Senhas em `char[]` podem ser apagadas da memória após o uso.  
   - `JPasswordField` mascara a entrada com asteriscos.  

⚠ **Limitações**:  
   - **Applets não confiáveis** não podem solicitar credenciais.  
   - APIs como `Authenticator` exigem permissões especiais em ambientes restritos.  

🔧 **Personalização**:  
   - Substitua `getPasswordAuthentication()` para buscar credenciais de um arquivo, banco de dados, etc.  



Com `Authenticator` e `JPasswordField`, é possível acessar sites HTTP protegidos de forma segura e integrada ao Java. Para autenticação baseada em cookies/formulários, são necessárias técnicas mais avançadas (ex: `CookieHandler` e `URLConnection`).

