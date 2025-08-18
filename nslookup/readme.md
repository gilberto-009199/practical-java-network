>Eu, esse e um capitulo aonde varias coisas não funcionarão como mencionado no livro
> O livro e antigo, mais seus conceitos e ate example na maior funciona bem


### Capitulo 4 - Internet Addresses

Ele faz um longa descrição sobre como opera o ipv4 e o ipv6 , mas e o memsoque do capitulo 1. Depois começa a falar da classe `InetAddress`.

#### The InetAddress Class

A classe `java.net.InetAddress` é a representação de alto nível de um endereço IP (IPv4 ou IPv6) em Java. Ela é amplamente utilizada por outras classes de rede, como `Socket`, `ServerSocket`, `URL`, `DatagramSocket`, entre outras. Um objeto `InetAddress` geralmente contém um **hostname** e um **endereço IP**.


Não há construtores públicos em `InetAddress`. Em vez disso, usamos **métodos estáticos** (factory methods) que consultam o DNS para resolver um hostname.

**1. `getByName(String host)`**

Consulta o DNS para obter o endereço IP associado a um hostname.
- Se o hostname não for encontrado, lança `UnknownHostException`.
- Pode retornar um endereço em cache (sem consultar o DNS novamente).

**Exemplo:**
```java
try {
    InetAddress address = InetAddress.getByName("www.oreilly.com");
    System.out.println(address); // Saída: www.oreilly.com/208.201.239.36
} catch (UnknownHostException ex) {
    System.err.println("Host não encontrado.");
}
```

**2. `getAllByName(String host)`**

Retorna **todos os endereços IP** associados a um hostname (útil para hosts com múltiplos IPs).

**Exemplo:**
```java
try {
    InetAddress[] addresses = InetAddress.getAllByName("www.google.com");
    for (InetAddress addr : addresses) {
        System.out.println(addr);
    }
} catch (UnknownHostException ex) {
    System.err.println("Host não encontrado.");
}
```


**3. `getLocalHost()`**

Retorna o endereço IP da máquina local.
- Se não houver conexão com DNS, retorna o **loopback** (`localhost/127.0.0.1`).

**Exemplo:**
```java
InetAddress local = InetAddress.getLocalHost();
System.out.println(local); // Exemplo: meu-pc/192.168.1.100
```


+ **4. `getByAddress(byte[] addr)` e `getByAddress(String hostname, byte[] addr)`**  
  Cria um `InetAddress` a partir de um **array de bytes** (IPv4: 4 bytes, IPv6: 16 bytes).
- Não consulta o DNS, útil para endereços não registrados ou redes locais.
- O segundo método permite associar um hostname ao IP.

**Exemplo:**
```java
byte[] ip = {192, 168, 1, 1};
InetAddress address = InetAddress.getByAddress(ip);
InetAddress namedAddress = InetAddress.getByAddress("meu-servidor", ip);
```


**Métodos Principais**

| Método                     | Descrição                                                    |
| -------------------------- | ------------------------------------------------------------ |
| `getHostName()`            | Retorna o hostname (faz consulta DNS reversa se necessário). |
| `getHostAddress()`         | Retorna o IP no formato `String` (ex: `"192.168.1.1"`).      |
| `isReachable(int timeout)` | Testa se o host está acessível (ping).                       |

**Exemplo de uso:**

```java
InetAddress google = InetAddress.getByName("www.google.com");
System.out.println(google.getHostName()); // www.google.com
System.out.println(google.getHostAddress()); // 172.217.0.132
System.out.println(google.isReachable(1000)); // true ou false
```


**Tratamento de Exceções**
- `UnknownHostException`: Lançada quando o host não pode ser resolvido.
- `SecurityException`: Pode ocorrer se o código estiver em um ambiente restrito (ex.: applets).

**Exemplo seguro:**
```java
try {
    InetAddress address = InetAddress.getByName("host-desconhecido");
} catch (UnknownHostException ex) {
    System.err.println("Host não encontrado: " + ex.getMessage());
}
```

**Cenários de Uso**
1. **Resolução de DNS:** Converter hostnames em IPs e vice-versa.
2. **Verificação de conectividade:** Usar `isReachable()` para testar acessibilidade.
3. **Redes locais:** Criar endereços para dispositivos não registrados em DNS (ex.: `getByAddress`).

A classe `InetAddress` é essencial para trabalhar com endereços IP em Java, oferecendo métodos para:
- Consultar DNS (`getByName`, `getAllByName`).
- Obter o endereço local (`getLocalHost`).
- Criar endereços manualmente (`getByAddress`).

Use-a em aplicações de rede para resolver hostnames, testar conectividade ou gerenciar endereços em ambientes sem DNS.


##### Caching

A classe `InetAddress` em Java implementa **cache de consultas DNS** para melhorar desempenho, evitando repetir buscas desnecessárias. Aqui estão os pontos-chave:


**1. Cache de Resultados Positivos**

- **O que é armazenado**: Endereços IP de hosts resolvidos com sucesso.
- **Tempo padrão**:
    - O cache **nunca expira** por padrão (resultados positivos são mantidos indefinidamente enquanto a JVM estiver em execução).
    - Pode ser alterado pela propriedade do sistema:
      ```java
      java.security.Security.setProperty("networkaddress.cache.ttl", "60"); // Expira em 60 segundos
      ```
    - Valores especiais:
        - **`0`**: Desativa o cache (sempre consulta o DNS).
        - **`-1`**: Cache "eterno" (padrão).


**2. Cache de Resultados Negativos (Falhas)**

- **O que é armazenado**: Falhas em consultas DNS (ex.: `UnknownHostException`).
- **Tempo padrão**:
    - **10 segundos** (para evitar bloquear repetidamente um host temporariamente inacessível).
    - Configurável via:
      ```java
      java.security.Security.setProperty("networkaddress.cache.negative.ttl", "30"); // Expira em 30 segundos
      ```
    - Valores especiais:
        - **`0`**: Sem cache (sempre repete a consulta).
        - **`-1`**: Cache "eterno" (não recomendado para falhas).


**3. Cenários e Impactos**

- **Problema com IPs dinâmicos**:  
  Se um host mudar seu IP durante a execução do programa, o cache pode retornar o valor antigo.
    - **Solução**: Redefina o TTL (Time-To-Live) para um valor baixo ou `0` em ambientes com IPs voláteis.

- **Falhas temporárias**:  
  Um host pode ficar inacessível brevemente (ex.: timeout de DNS), mas o cache de falhas pode impedir tentativas imediatas.
    - **Solução**: Reduza `networkaddress.cache.negative.ttl` para reagir mais rápido a recuperações.


**4. Cache em Outros Níveis**

Além do cache da JVM:
- **Sistema operacional**: Mantém seu próprio cache DNS (ex.: `nscd` no Linux).
- **Servidores DNS intermediários**: Podem cachear respostas por horas ou dias.
    - **Implicação**: Mudanças de IP podem demorar a se propagar globalmente.


**5. Exemplo de Configuração**

```java
import java.net.InetAddress;
import java.security.Security;

public class DNSCacheExample {
    public static void main(String[] args) throws Exception {
        // Configura cache para expirar em 30 segundos (sucessos) e 5 segundos (falhas)
        Security.setProperty("networkaddress.cache.ttl", "30");
        Security.setProperty("networkaddress.cache.negative.ttl", "5");

        // Primeira consulta (pode ir ao DNS)
        InetAddress addr1 = InetAddress.getByName("www.google.com");
        System.out.println(addr1);

        // Segunda consulta (usará cache, se dentro do TTL)
        InetAddress addr2 = InetAddress.getByName("www.google.com");
        System.out.println(addr2);
    }
}
```


**6. Boas Práticas**

- **Ambientes dinâmicos**:  
  Reduza o TTL (ex.: 30 segundos) para aplicações que dependem de atualizações frequentes de DNS.
- **Ambientes estáticos**:  
  Mantenha o padrão (`-1`) para maximizar desempenho.
- **Depuração**:  
  Desative o cache temporariamente (`TTL = 0`) para testar problemas de resolução de nomes.

O cache de `InetAddress` é crucial para **evitar consultas DNS repetidas**, mas requer configuração cuidadosa em cenários onde:
- **IPs mudam frequentemente** (ex.: cloud scaling).
- **Falhas temporárias devem ser rapidamente reavaliadas**.

Use as propriedades `networkaddress.cache.ttl` e `networkaddress.cache.negative.ttl` para ajustar o comportamento conforme necessário.


##### Lookups by IP address


**Como `getByName()` Funciona com IPs?**

Quando você passa um **endereço IP** (ex: "192.168.1.1") para `InetAddress.getByName()`:
1. **Não há consulta DNS inicial**:
    - Java cria um objeto `InetAddress` imediatamente, **sem verificar se o IP existe ou é alcançável**.
    - O "hostname" temporário é definido como o próprio IP (ex: `"192.168.1.1"`).

2. **Consulta DNS só ocorre quando necessário**:
    - Se você chamar `getHostName()`, o Java **só então** faz uma **busca reversa de DNS** para resolver o nome do host.
    - Se a busca reversa falhar, o hostname permanece como o IP original (sem lançar exceção).

**Exemplo Prático**:

```java
InetAddress ipOnly = InetAddress.getByName("8.8.8.8"); // Sem DNS aqui!
System.out.println(ipOnly); // Saída: /8.8.8.8 (hostname ainda não resolvido)

// Solicita o hostname (agora faz DNS reverso)
System.out.println(ipOnly.getHostName()); // Saída: dns.google (resolvido via DNS)
```


**Por Que Preferir Hostnames a IPs?**
1. **Estabilidade**:
    - Hostnames (ex: `www.oreilly.com`) raramente mudam, enquanto IPs podem ser alterados (ex: balanceamento de carga, migração de servidores).

2. **Flexibilidade**:
    - Um hostname pode representar múltiplos IPs (útil para redundância e CDNs).

3. **Legibilidade**:
    - `www.google.com` é mais intuitivo que `142.250.218.68`.


**Quando Usar IPs Diretamente?**:
- **Ambientes controlados**: Redes locais onde hosts não têm nomes DNS.
- **Performance crítica**: Evitar atrasos de resolução DNS (em aplicações de baixa latência).
- **Falha em DNS**: Se o sistema de nomes estiver indisponível.


**Cuidados Importantes**:
1. **IPs inválidos ou inacessíveis**:
    - `InetAddress.getByName("999.999.999.999")` cria o objeto, mas tentativas de conexão falharão.
    - Use `isReachable()` para testar acessibilidade:
      ```java
      InetAddress ip = InetAddress.getByName("192.168.1.1");
      System.out.println(ip.isReachable(1000)); // true/false
      ```  

2. **Busca reversa pode falhar silenciosamente**:
    - Se `getHostName()` não resolver o nome, retorna o IP em formato `String` (sem exceção).

**Exemplo Completo**:

```java
import java.net.InetAddress;

public class IPLookup {
    public static void main(String[] args) throws Exception {
        // Cria InetAddress sem DNS (apenas IP)
        InetAddress ip = InetAddress.getByName("142.250.218.68");

        System.out.println("IP: " + ip.getHostAddress()); // 142.250.218.68
        System.out.println("Hostname inicial: " + ip.getHostName()); // 142.250.218.68

        // Força busca reversa
        String hostname = ip.getHostName();
        System.out.println("Hostname resolvido: " + hostname); // ex: gru06s25-in-f4.1e100.net

        // Teste de conectividade
        System.out.println("Alcançável? " + ip.isReachable(1000)); // true/false
    }
}
```


- **Prefira hostnames** em aplicações genéricas (são mais estáveis e legíveis).
- **Use IPs diretamente** apenas quando necessário (ex.: redes internas, otimizações).
- Lembre-se: `getByName()` com IP **não valida** a existência do host — isso só ocorre em operações posteriores (como `getHostName()` ou tentativas de conexão).

> **Dica**: Para verificar a existência de um host, combine `InetAddress` com `isReachable()` ou tente uma conexão real (ex.: `Socket`).


##### Security issues

A criação de objetos InetAddress a partir de nomes de host é considerada uma operação potencialmente insegura porque envolve consultas DNS. Isso representa riscos de segurança, especialmente para código não confiável como applets Java.

**Restrições para Código Não Confiável**
- Só pode obter IPs:
    - Do host de origem (codebase)
    - Do localhost (retornando sempre 127.0.0.1)
- Não pode:
    - Fazer consultas DNS arbitrárias
    - Descobrir o verdadeiro hostname/IP local
- Permite criar InetAddress apenas a partir de strings IP (sem DNS)

**Riscos de Consultas DNS Arbitrárias**  
Podem ser usadas para criar canais ocultos de comunicação, onde:
1. Dados são codificados em subdomínios (ex: "dados-secretos.ataque.com")
2. O atacante monitora logs DNS para extrair informações
3. Mesmo hosts inexistentes podem vazar dados via mensagens de erro

**Mecanismo de Proteção**
- SecurityManager usa checkConnect():
    - Porta -1: verifica permissão para resolução DNS
    - Porta > -1: verifica permissão para conexão
- Para código confiável, restrições podem ser relaxadas via:
    - Assinatura digital
    - Arquivos de política de segurança

**Boas Práticas**
- Código não confiável deve usar IPs literais quando possível
- getLocalHost() em ambientes restritos retorna apenas localhost
- Consultas DNS devem ser validadas e monitoradas

O Java implementa restrições rígidas para consultas DNS em código não confiável, prevenindo vazamento de informações e ataques via resolução de nomes.
Essas proteções são fundamentais para segurança em ambientes como applets e aplicações sandboxed.


##### Getter Methods

A classe `InetAddress` possui quatro métodos principais para obtenção de informações:

1. **getHostName()**
    - Retorna o nome do host associado ao endereço IP
    - Se não conseguir resolver, retorna o IP no formato quad (ex: "192.168.1.1")
    - Exemplo:
      ```java
      InetAddress local = InetAddress.getLocalHost();
      System.out.println(local.getHostName()); // Nome do host ou IP
      ```

2. **getCanonicalHostName()**
    - Versão mais agressiva que sempre tenta resolver o nome canônico via DNS
    - Útil quando se parte de um IP para descobrir o nome completo
    - Exemplo:
      ```java
      InetAddress ia = InetAddress.getByName("8.8.8.8");
      System.out.println(ia.getCanonicalHostName()); // dns.google
      ```

3. **getHostAddress()**
    - Retorna o endereço IP no formato string (IPv4 ou IPv6)
    - Exemplo:
      ```java
      System.out.println(InetAddress.getLocalHost().getHostAddress());
      ```

4. **getAddress()**
    - Retorna o IP como array de bytes (ordem de rede)
    - Requer tratamento especial pois bytes em Java são signed (-128 a 127)
    - Conversão para valor unsigned:
      ```java
      byte[] address = ia.getAddress();
      int unsignedByte = address[i] < 0 ? address[i] + 256 : address[i];
      ```

**Imutabilidade e Thread Safety**

A classe é imutável (sem métodos setter), garantindo segurança em ambientes multi-thread.

**Identificação IPv4 vs IPv6**

Verifique o tamanho do array retornado por `getAddress()`:
```java
if (address.length == 4) {
    System.out.println("IPv4");
} else if (address.length == 16) {
    System.out.println("IPv6");
}
```

**Exemplo Prático**

```java
InetAddress google = InetAddress.getByName("www.google.com");
System.out.println("Nome: " + google.getHostName());
System.out.println("IP: " + google.getHostAddress());
System.out.println("Bytes: " + Arrays.toString(google.getAddress()));
```

Estes métodos fornecem diferentes níveis de acesso às informações de rede, balanceando entre desempenho (cache) e atualização (consultas DNS).


##### Address Types


A classe `InetAddress` em Java fornece métodos para identificar tipos específicos de endereços IP:

###### **Métodos de Verificação**

1. `isAnyLocalAddress()`
    - Verifica se é um endereço **wildcard**(0.0.0.0 em IPv4 ou \:\: em - IPv6)
    - , que representa qualquer interface local.

2. **`isLoopbackAddress()`**
    - Identifica endereços de **loopback** (127.0.0.1 em IPv4 ou \:\:1 em IPv6), usados para comunicação interna na máquina.

3. **`isLinkLocalAddress()`**
    - Retorna `true` para endereços IPv6 **link-local** (iniciam com FE80\:\:), usados em redes locais sem roteamento externo.

4. **`isSiteLocalAddress()`**
    - Detecta endereços IPv6 **site-local** (iniciam com FEC0\:\:), restritos a uma rede corporativa/campus.

5. **`isMulticastAddress()`**
    - Verifica se é um endereço **multicast** (224.0.0.0 a 239.255.255.255 em IPv4 ou FF00\:\:/8 em IPv6), para envio a múltiplos hosts.

###### **Subtipos de Multicast**
- `isMCGlobal()`: Escopo global (ex.: FF0E\:\: em IPv6).
- `isMCOrgLocal()`: Restrito a uma organização (ex.: FF08\:\: ).
- `isMCSiteLocal()`: Limitado a um site (ex.: FF05\:\:).
- `isMCLinkLocal()`: Apenas na sub-rede local (ex.: FF02\:\:).
- `isMCNodeLocal()`: Restrito à interface de rede (ex.: FF01\:\:).

###### **Exemplo Prático**

O programa abaixo testa as características de um endereço IP:
```java
InetAddress address = InetAddress.getByName("224.0.2.1");
if (address.isMulticastAddress()) {
    System.out.println("É multicast!");
    if (address.isMCGlobal()) {
        System.out.println("Escopo global");
    }
}
```

###### **Saídas Típicas**
- **Loopback**:
  ```
  /127.0.0.1 is loopback address.
  ```
- **Site-local (IPv4 privado)**:
  ```
  /192.168.1.1 is a site-local address.
  ```
- **Multicast global**:
  ```
  /224.0.2.1 is a global multicast address.
  ```

###### **Implicações**

- **Segurança**: Endereços locais (link/site) não são roteados externamente.
- **Redes IPv6**: Autoconfiguração via `FE80::` (link-local) é comum.
- **Debug**: Loopback evita dependência de hardware de rede.

Use esses métodos para validar endereços em aplicações de rede, garantindo comportamentos esperados (ex.: evitar roteamento de tráfego local para a internet).

##### Testing Reachability


A classe `InetAddress` em Java oferece métodos para verificar se um host está acessível na rede:

###### **Métodos Disponíveis**
1. **`isReachable(int timeout)`**
    - Verifica se o host responde dentro de um tempo limite (`timeout` em milissegundos).
    - Usa **ICMP Echo Request** (similar ao comando `ping`).
    - Retorna `true` se o host estiver alcançável, `false` caso contrário.
    - Lança `IOException` em erros de rede.

   **Exemplo:**
   ```java
   InetAddress host = InetAddress.getByName("example.com");
   boolean reachable = host.isReachable(5000); // 5 segundos de timeout
   System.out.println("Alcançável? " + reachable);
   ```

2. **`isReachable(NetworkInterface interface, int ttl, int timeout)`**
    - Permite especificar:
        - **`interface`**: A placa de rede local a ser usada (útil em máquinas com múltiplas interfaces).
        - **`ttl`** (Time-To-Live): Número máximo de saltos (hops) antes do pacote ser descartado.
    - Mais preciso para redes complexas ou multihomed.

   **Exemplo:**
   ```java
   NetworkInterface eth = NetworkInterface.getByName("eth0");
   boolean reachable = host.isReachable(eth, 64, 3000); // TTL=64, timeout=3s
   ```

---

###### **Casos de Uso**
- **Verificar disponibilidade de servidores** antes de conectar.
- **Diagnóstico de redes**: Identificar se falhas são devido a firewalls, roteadores ou hosts offline.
- **Balanceamento de carga**: Testar qual rota/interface tem melhor resposta.

---

###### **Limitações**
- **Firewalls/ICMP bloqueado**: Muitas redes bloqueiam pacotes ICMP, fazendo o método retornar `false` mesmo para hosts ativos.
- **TTL**: Um valor baixo pode não alcançar hosts distantes.
- **IPv6**: Requer configurações específicas em algumas redes.


###### **Exemplo Completo**
```java
import java.net.*;
import java.io.IOException;

public class TestReachability {
    public static void main(String[] args) {
        try {
            InetAddress google = InetAddress.getByName("google.com");
            boolean reachable = google.isReachable(3000); // Timeout de 3s
            System.out.println("Google está alcançável? " + reachable);
        } catch (IOException e) {
            System.err.println("Erro de rede: " + e.getMessage());
        }
    }
}
```

**Saída:**
```
Google está alcançável? true
```


###### **Alternativas**

Se `isReachable()` não for confiável (devido a bloqueios de ICMP), considere:
- **Conexões TCP** (ex.: `Socket` na porta 80).
- **Bibliotecas externas** como Apache Commons Net (`ping` customizado).

Use esses métodos para validar conectividade em aplicações críticas, mas sempre com tratamento de erros e fallbacks.


##### Object Methods

A classe `InetAddress` sobrescreve três métodos fundamentais de `Object` para fornecer comportamentos específicos:

###### **1. `equals(Object o)`**
- **Comparação por endereço IP**: Dois objetos `InetAddress` são considerados iguais se:
    - Ambos são instâncias de `InetAddress`.
    - Possuem o **mesmo endereço IP** (ignorando o hostname).
- **Exemplo**:
  ```java
  InetAddress ibiblio = InetAddress.getByName("www.ibiblio.org");
  InetAddress helios = InetAddress.getByName("helios.ibiblio.org");
  System.out.println(ibiblio.equals(helios)); // true (mesmo IP)
  ```

###### **2. `hashCode()`**
- **Baseado no IP**: O código hash é calculado apenas a partir do endereço IP, não do hostname.
- **Consistência com `equals()`**: Objetos com o mesmo IP retornam o mesmo hash code.
  ```java
  System.out.println(ibiblio.hashCode() == helios.hashCode()); // true
  ```

###### **3. `toString()`**
- **Formato padrão**: Retorna uma string no formato `hostname/IP`.
    - Se o hostname não existir (Java 1.4+): `"/IP"` (hostname vazio).
    - Exemplo:
      ```java
      System.out.println(InetAddress.getByName("8.8.8.8")); // "/8.8.8.8"
      System.out.println(InetAddress.getByName("google.com")); // "google.com/142.250.218.46"
      ```

###### **Exemplo Completo**

```java
import java.net.*;

public class ExemploInetAddress {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress host1 = InetAddress.getByName("www.google.com");
        InetAddress host2 = InetAddress.getByName("142.250.218.46");

        // equals()
        System.out.println("Mesmo IP? " + host1.equals(host2)); // true

        // hashCode()
        System.out.println("Hash codes iguais? " + (host1.hashCode() == host2.hashCode())); // true

        // toString()
        System.out.println("Host1: " + host1); // www.google.com/142.250.218.46
        System.out.println("Host2: " + host2); // /142.250.218.46
    }
}
```

###### **Casos de Uso Práticos**

1. **Comparação de hosts**:  
   Verificar se dois nomes (ex.: `www.site.com` e `cdn.site.com`) apontam para o mesmo servidor.
2. **Tabelas hash**:  
   Usar `InetAddress` como chave em `HashMap` (funciona porque `hashCode()` é baseado no IP).
3. **Logs**:  
   `toString()` facilita a exibição de informações de conexão em logs.

###### **Observações Importantes**

- **Hostnames diferentes, mesmo IP**:
  ```java
  InetAddress a = InetAddress.getByName("site.com");
  InetAddress b = InetAddress.getByName("198.51.100.42");
  System.out.println(a.equals(b)); // true (se o IP for o mesmo)
  ```
- **IPv6**: Os métodos funcionam da mesma forma para endereços IPv6.

Esses métodos garantem que `InetAddress` seja consistente com o contrato básico de `Object`, enquanto adicionam semântica específica para endereços de rede.

#### The NetworkInterface Class

A classe `InetAddress` sobrescreve três métodos fundamentais de `Object` para fornecer comportamentos específicos:

##### **1. `equals(Object o)`**
- **Comparação por endereço IP**: Dois objetos `InetAddress` são considerados iguais se:
    - Ambos são instâncias de `InetAddress`.
    - Possuem o **mesmo endereço IP** (ignorando o hostname).
- **Exemplo**:
  ```java
  InetAddress ibiblio = InetAddress.getByName("www.ibiblio.org");
  InetAddress helios = InetAddress.getByName("helios.ibiblio.org");
  System.out.println(ibiblio.equals(helios)); // true (mesmo IP)
  ```

##### **2. `hashCode()`**
- **Baseado no IP**: O código hash é calculado apenas a partir do endereço IP, não do hostname.
- **Consistência com `equals()`**: Objetos com o mesmo IP retornam o mesmo hash code.
  ```java
  System.out.println(ibiblio.hashCode() == helios.hashCode()); // true
  ```

##### **3. `toString()`**
- **Formato padrão**: Retorna uma string no formato `hostname/IP`.
    - Se o hostname não existir (Java 1.4+): `"/IP"` (hostname vazio).
    - Exemplo:
      ```java
      System.out.println(InetAddress.getByName("8.8.8.8")); // "/8.8.8.8"
      System.out.println(InetAddress.getByName("google.com")); // "google.com/142.250.218.46"
      ```

**Exemplo Completo**

```java
import java.net.*;

public class ExemploInetAddress {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress host1 = InetAddress.getByName("www.google.com");
        InetAddress host2 = InetAddress.getByName("142.250.218.46");

        // equals()
        System.out.println("Mesmo IP? " + host1.equals(host2)); // true

        // hashCode()
        System.out.println("Hash codes iguais? " + (host1.hashCode() == host2.hashCode())); // true

        // toString()
        System.out.println("Host1: " + host1); // www.google.com/142.250.218.46
        System.out.println("Host2: " + host2); // /142.250.218.46
    }
}
```


##### **Casos de Uso Práticos**
1. **Comparação de hosts**:  
   Verificar se dois nomes (ex.: `www.site.com` e `cdn.site.com`) apontam para o mesmo servidor.
2. **Tabelas hash**:  
   Usar `InetAddress` como chave em `HashMap` (funciona porque `hashCode()` é baseado no IP).
3. **Logs**:  
   `toString()` facilita a exibição de informações de conexão em logs.

**Observações Importantes**

- **Hostnames diferentes, mesmo IP**:
  ```java
  InetAddress a = InetAddress.getByName("site.com");
  InetAddress b = InetAddress.getByName("198.51.100.42");
  System.out.println(a.equals(b)); // true (se o IP for o mesmo)
  ```
- **IPv6**: Os métodos funcionam da mesma forma para endereços IPv6.

Esses métodos garantem que `InetAddress` seja consistente com o contrato básico de `Object`, enquanto adicionam semântica específica para endereços de rede.


##### Inet4Address and Inet6Address

A classe `NetworkInterface` em Java representa uma **interface de rede local**, que pode ser:
- **Física** (ex.: placa de Ethernet, Wi-Fi)
- **Virtual** (ex.: endereços IP adicionais vinculados ao mesmo hardware)

Ela permite enumerar e manipular todos os endereços IP locais, independentemente da interface.

###### **Principais Métodos de Fábrica**

São usados para obter objetos `NetworkInterface`:

###### **1. `getByName(String name)`**

- Retorna a interface pelo nome (ex.: `"eth0"` no Linux, `"CE31"` no Windows).
- Retorna `null` se a interface não existir.
- Pode lançar `SocketException` em erros de rede.

**Exemplo (Unix/Linux):**
```java
try {
    NetworkInterface ni = NetworkInterface.getByName("eth0");
    if (ni == null) {
        System.err.println("Interface eth0 não encontrada.");
    }
} catch (SocketException ex) {
    System.err.println("Erro ao acessar interfaces.");
}
```

###### **2. `getByInetAddress(InetAddress address)`**

- Retorna a interface associada a um endereço IP específico.
- Útil para verificar qual interface está vinculada a um IP local.

**Exemplo (Loopback):**
```java
try {
    InetAddress loopback = InetAddress.getByName("127.0.0.1");
    NetworkInterface ni = NetworkInterface.getByInetAddress(loopback);
    if (ni == null) {
        System.err.println("Loopback não configurado!");
    }
} catch (SocketException | UnknownHostException ex) {
    System.err.println("Erro: " + ex.getMessage());
}
```

###### **3. `getNetworkInterfaces()`**
- Retorna uma **enumeração** (`Enumeration`) de todas as interfaces do sistema.
- Ideal para listar interfaces ativas.

**Exemplo (Listar Todas as Interfaces):**
```java
Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
while (interfaces.hasMoreElements()) {
    NetworkInterface ni = interfaces.nextElement();
    System.out.println(ni.getName() + " - " + Collections.list(ni.getInetAddresses()));
}
```

**Saída Típica (Linux):**
```
eth0 - [/192.168.1.100]
lo - [/127.0.0.1]
wlan0 - [/10.0.0.15]
```

###### **Casos de Uso Comuns**
1. **Identificar Interfaces de Rede**:
    - Descobrir quantas placas de rede estão ativas.
2. **Vincular Sockets a Interfaces Específicas**:
    - Útil para servidores com múltiplos IPs.
3. **Diagnóstico de Rede**:
    - Verificar se um IP está configurado corretamente em uma interface.

###### **Detalhes Importantes**

- **Nomes de Interfaces**:
    - **Unix/Linux**: `eth0`, `wlan0`, `lo` (loopback).
    - **Windows**: Nomes baseados no hardware (ex.: `"Realtek PCIe GbE Family Controller"`).
- **Endereços IP por Interface**:
    - Use `ni.getInetAddresses()` para listar os IPs de uma interface.
- **Imutabilidade**:
    - Os objetos `NetworkInterface` são imutáveis e seguros para threads.

###### **Exemplo Completo**

```java
import java.net.*;
import java.util.*;

public class ListInterfaces {
    public static void main(String[] args) throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            System.out.println("Interface: " + ni.getName());
            System.out.println("  Endereços: " + Collections.list(ni.getInetAddresses()));
        }
    }
}
```

###### **Conclusão**
A classe `NetworkInterface` é essencial para:
- Gerenciar **múltiplos IPs** em um mesmo host.
- Diagnosticar problemas de rede.
- Criar aplicações que dependem de interfaces específicas (ex.: servidores multi-homed).

Use os métodos de fábrica para acessar interfaces de forma segura e eficiente!

#### Getter Methods

A classe `NetworkInterface` fornece métodos para obter informações sobre interfaces de rede locais. Após obter um objeto `NetworkInterface`, você pode acessar:

##### **1. `getInetAddresses()`**
- Retorna uma **enumeração** (`Enumeration<InetAddress>`) com todos os endereços IP vinculados à interface.
- Útil para interfaces com múltiplos IPs (ex.: configurações IPv4 + IPv6).

**Exemplo:**
```java
NetworkInterface eth0 = NetworkInterface.getByName("eth0");
Enumeration<InetAddress> addresses = eth0.getInetAddresses();
while (addresses.hasMoreElements()) {
    System.out.println("IP: " + addresses.nextElement());
}
```
**Saída:**
```
IP: /192.168.1.100
IP: /fe80:0:0:0:1a2b:3c4d:5e6f:7a8b%eth0
```

##### **2. `getName()`**
- Retorna o **nome técnico** da interface (ex.: `"eth0"` no Linux, `"wlan0"` para Wi-Fi).

**Exemplo:**
```java
System.out.println("Nome da interface: " + eth0.getName()); // eth0
```

##### **3. `getDisplayName()`**
- Retorna um **nome amigável** (depende do sistema operacional):
    - **Linux/Unix**: Geralmente igual a `getName()` (ex.: `"eth0"`).
    - **Windows**: Nomes descritivos como `"Conexão Local"` ou `"Wi-Fi"`.

**Exemplo:**
```java
System.out.println("Nome amigável: " + eth0.getDisplayName()); 
```
**Saída no Windows:**
```
Nome amigável: Conexão Local
```

##### **Casos de Uso**

- **Listar IPs de uma interface**:
  ```java
  Collections.list(eth0.getInetAddresses()).forEach(System.out::println);
  ```
- **Identificar interfaces ativas**:
  ```java
  NetworkInterface.getNetworkInterfaces()
      .forEachRemaining(ni -> System.out.println(ni.getName()));
  ```

##### **Observações**
- **Múltiplos IPs**: Uma única interface pode ter vários endereços (IPv4 + IPv6 + aliases).
- **Nomes amigáveis**: `getDisplayName()` pode não ser tão "amigável" em sistemas Unix.
- **Segurança**: Os objetos são imutáveis e seguros para uso em múltiplas threads.

Use esses métodos para diagnosticar redes ou vincular conexões a interfaces específicas!


#### Some Useful Programs

Nesse trecho e encinado usar um ferramenta de consulta anti spam, mas ao oque me parece essa ferramenta não existe mais, oque encontrei na minha busca foi: https://check.spamhaus.org/results?query=

##### **1. Verificação de Spam (`SpamCheck`)**
- **Objetivo**: Identificar se um IP está em listas de spammers (como `sbl.spamhaus.org`).
- **Como funciona**:
    1. Inverte o IP (ex.: `207.87.34.17` → `17.34.87.207`).
    2. Concatena com o domínio da lista negra (ex.: `17.34.87.207.sbl.spamhaus.org`).
    3. Se a consulta DNS retornar `127.0.0.2`, o IP é um spammer.
- **Exemplo**:
  ```java
  boolean isSpammer = InetAddress.getByName("17.34.87.207.sbl.spamhaus.org") != null;
  ```
- **Cuidados**:
    - Servidores de lista negra podem mudar de endereço ou sofrer ataques DDoS.
    - Algumas listas usam `127.0.0.1` em vez de `127.0.0.2`.

---

##### **2. Processamento de Logs de Servidor Web (`Weblog` e `PooledWeblog`)**
- **Problema**: Logs de servidores web armazenam IPs, mas hostnames são mais úteis para análise.
- **Solução**:
    - **Versão sequencial (`Weblog`)**:
        - Lê o arquivo de log linha por linha.
        - Extrai o IP (tudo antes do primeiro espaço).
        - Usa `InetAddress.getByName(ip).getHostName()` para resolver o hostname.
        - **Desvantagem**: Lento devido a consultas DNS sequenciais.

    - **Versão paralela (`PooledWeblog`)**:
        - Usa um **pool de threads** (ex.: 4 threads) para resolver hostnames em paralelo.
        - **Ganho de desempenho**: 4x a 50x mais rápido que a versão sequencial.
        - Mantém a ordem original do log usando `Future` e uma fila de resultados.

- **Formato de Log (Common Log Format)**:
  ```
  205.160.186.76 unknown - [17/Jun/2013:22:53:58 -0500] "GET /bgs/greenbg.gif HTTP 1.0" 200 50
  ```  
    - O primeiro campo é o IP/hostname.

- **Exemplo de Código (PooledWeblog)**:
  ```java
  ExecutorService executor = Executors.newFixedThreadPool(4);
  Queue<LogEntry> results = new LinkedList<>();
  // Para cada linha do log:
  Future<String> future = executor.submit(new LookupTask(linha));
  results.add(new LogEntry(linha, future));
  ```

##### **Pontos-Chave**
- **DNS Reverso**: Usado para verificar spammers e resolver hostnames em logs.
- **Cache do `InetAddress`**: Acelera consultas repetidas ao mesmo IP.
- **Multithreading**: Essencial para tarefas de rede (como DNS) com alta latência.
- **Eficiência**:
    - Processar logs offline evita sobrecarregar o servidor web.
    - Thread pools evitam criar milhares de threads (problemas de memória).

##### **Exemplo Prático**

Para usar `PooledWeblog`:
```bash
java PooledWeblog access.log
```
**Saída**:
```
example.com - [17/Jun/2013:22:53:58 -0500] "GET /bgs/greenbg.gif HTTP 1.0" 200 50
```

##### **Conclusão**

A classe `InetAddress` permite:
- **Combate a spam** via consultas DNS inteligentes.
- **Otimização de logs** web com processamento paralelo.  
  Ambos os casos mostram como operações de rede podem ser aceleradas com cache e multithreading.
