## Nonblocking I/O

>Eu,
>Isso aqui e muito importante ler e compreender, se necessario leia 2, 3 vezes


**O Desafio da Lentidão das Redes**

Redes são significativamente mais lentas que outros componentes de hardware:
- **CPU/Memória**: Até 6 GB/s
- **Discos**: ~150 MB/s
- **Redes Locais (LAN)**: ~150 MB/s (teórico), mas muitas operam 10-100x mais devagar
- **Internet**: Exemplo: conexão FIOS de 6 MB/s (apenas 5% da velocidade da LAN)

**Soluções Tradicionais e Seus Limites**

Java tradicionalmente usa:
- **Buffering** (armazenamento temporário)
- **Multithreading** (múltiplas threads para conexões simultâneas)

**Problemas**:

- Cada thread consome ~1 MB de RAM
- Overhead significativo em servidores com milhares de conexões

**A Revolução do NIO**

Introduzido no Java 1.4, o pacote `java.nio` oferece:
- **Single-thread para múltiplas conexões**: Uma thread gerencia vários canais de E/S
- **Operações não bloqueantes**: A thread não fica esperando por operações de rede
- **Eficiência**: Ideal para cenários de alta concorrência

**Quando Usar NIO?**

1. **Servidores com +10k conexões simultâneas** (ex: sistemas de coleta de transações em rede de lojas)
2. **Conexões persistentes com baixo tráfego**

**Limitações Importantes**

- **Complexidade**: Código mais difícil de implementar
- **Desempenho**: Em testes no Java 6 (Linux), I/O tradicional foi 30% mais rápido
- **Disponibilidade**: Não suportado em Java ME (mas presente no Android)

**Regras de Ouro para Otimização**

1. **Não otimize prematuramente**
2. **Só otimize após medições concretas** que:
    - Identifiquem o gargalo real
    - Comprovem a eficácia da solução

**Conclusão**

Enquanto o NIO é poderoso para cenários específicos de alta escala, para a maioria dos casos o modelo tradicional multithread ainda oferece melhor equilíbrio entre simplicidade e desempenho. A escolha deve ser sempre guiada por dados reais e necessidades específicas do projeto.

**Dica Final**: Antes de adotar NIO, avalie se a complexidade adicional justifica os ganhos no seu caso específico. Muitas vezes, a simplicidade do modelo tradicional é a melhor escolha.


##### An Example Client

**Exemplo de um Cliente com NIO**

Embora as APIs de I/O novas não sejam especificamente projetadas para clientes, elas funcionam para eles. Vou começar com um programa cliente usando as novas APIs NIO porque é um pouco mais simples. Muitos clientes podem ser implementados com uma conexão por vez, permitindo introduzir canais e buffers antes de abordar seletores e I/O não bloqueante.

**Protocolo Character Generator (RFC 864)**

Um cliente simples para o protocolo character generator (RFC 864) demonstrará o básico. Este protocolo de teste:
- Servidor escuta na porta 19
- Envia sequência contínua de caracteres até o cliente desconectar
- Ignora qualquer entrada do cliente
- Padrão comum: linhas de 72 caracteres ASCII com quebras de linha

**Por que usar este protocolo?**
- Simplicidade: não obscurece o I/O
- Gera tráfego intenso - bom para demonstrar NIO
- (Nota: raramente usado hoje, pode ser bloqueado por firewalls)

**Implementação do Cliente NIO**

1. **Criar SocketChannel**:
```java
SocketAddress rama = new InetSocketAddress("rama.poly.edu", 19);
SocketChannel client = SocketChannel.open(rama);  // Bloqueante por padrão
```

2. **Preparar Buffer**:
```java
ByteBuffer buffer = ByteBuffer.allocate(74);  // 74 bytes (72 chars + CRLF)
```

3. **Ler dados**:
```java
int bytesRead = client.read(buffer);  // Retorna bytes lidos ou -1
```

4. **Escrever saída**:
```java
WritableByteChannel output = Channels.newChannel(System.out);
buffer.flip();  // Prepara buffer para leitura
output.write(buffer);
buffer.clear();  // Limpa para reuso
```

**Fluxo Completo**:
1. Conecta ao servidor
2. Lê dados para o buffer
3. Escreve buffer na saída
4. Limpa e repete

**Otimização**:
- Reutilizar o mesmo buffer evita custos de alocação
- `flip()` alterna entre modos de leitura/escrita
- `clear()` prepara para novos dados sem realocação

**Modo Não-Bloqueante** (será abordado depois):
- Configurável com `configureBlocking(false)`
- Retorna 0 se nenhum dado disponível imediatamente

Este exemplo mostra o fluxo básico de E/S com NIO, embora ainda use operações bloqueantes. A verdadeira vantagem do NIO aparece com múltiplas conexões e operações não-bloqueantes, que serão demonstradas a seguir.

**Exemplo 11-1: Cliente CharGen baseado em canais**

O Exemplo 11-1 combina esses conceitos em um cliente completo. Como o protocolo chargen é infinito por design, você precisará encerrar o programa usando Ctrl-C.

```java
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.IOException;

public class ChargenClient {
    public static int DEFAULT_PORT = 19;
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java ChargenClient host [porta]");
            return;
        }
        
        int porta;
        try {
            porta = Integer.parseInt(args[1]);
        } catch (RuntimeException ex) {
            porta = DEFAULT_PORT;
        }
        
        try {
            SocketAddress endereço = new InetSocketAddress(args[0], porta);
            SocketChannel cliente = SocketChannel.open(endereço);
            ByteBuffer buffer = ByteBuffer.allocate(74);
            WritableByteChannel saida = Channels.newChannel(System.out);
            
            while (cliente.read(buffer) != -1) {
                buffer.flip();
                saida.write(buffer);
                buffer.clear();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
```

**Saída de exemplo:**
```
$ java ChargenClient rama.poly.edu
 !"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefg
!"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefgh
"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghi
...
```

**Modo Não-Bloqueante**

Para tornar a conexão não-bloqueante:
```java
cliente.configureBlocking(false);
```

Em modo não-bloqueante, `read()` pode retornar 0 quando não há dados disponíveis. O loop precisa ser ajustado:

```java
while (true) {
    // Código adicional pode ser executado aqui
    int n = cliente.read(buffer);
    
    if (n > 0) {
        buffer.flip();
        saida.write(buffer);
        buffer.clear();
    } else if (n == -1) {
        // Fim dos dados (improvável neste protocolo)
        break;
    }
}
```

**Aplicações práticas:**
- Em clientes com múltiplas conexões, isso permite processar conexões rápidas sem esperar pelas lentas
- Útil para adicionar verificações de cancelamento pelo usuário
- Permite que cada conexão opere em sua própria velocidade

Este exemplo mostra a transição entre o modelo bloqueante tradicional e a abordagem não-bloqueante do NIO, que será expandida na próxima seção para lidar com múltiplas conexões simultâneas.

##### An Example Server

>Eu, parei na pagina 377
>e na pagina 78

Clientes são bons e úteis, mas os canais e buffers são realmente destinados a sistemas de servidor que precisam processar muitas conexões simultâneas de forma eficiente. Para lidar com servidores, é necessário um terceiro novo componente, além dos buffers e canais usados para o cliente. Especificamente, você precisa de **seletores (selectors)**, que permitem ao servidor identificar todas as conexões que estão prontas para receber saída ou enviar entrada.

Para demonstrar o básico, este exemplo implementa um servidor simples para o protocolo **character generator**. Ao implementar um servidor que aproveita as novas APIs de E/S (I/O), comece chamando o método estático **ServerSocketChannel.open()** para criar um novo objeto **ServerSocketChannel**:

```java
ServerSocketChannel serverChannel = ServerSocketChannel.open();
```  

Inicialmente, esse canal não está realmente escutando em nenhuma porta. Para vinculá-lo a uma porta, recupere seu objeto **ServerSocket** associado usando o método **socket()** e, em seguida, use o método **bind()** nesse objeto. Por exemplo, este fragmento de código vincula o canal a um socket de servidor na porta 19:

```java
ServerSocket ss = serverChannel.socket();  
ss.bind(new InetSocketAddress(19));  
```  

No Java 7 e versões posteriores, você pode vincular diretamente sem recuperar o **java.net.ServerSocket** subjacente:

```java
serverChannel.bind(new InetSocketAddress(19));  
```  

Assim como com sockets de servidor comuns, vincular à porta 19 exige que você tenha privilégios de **root** no Unix (incluindo Linux e Mac OS X). Usuários sem privilégios de root só podem vincular a portas a partir da 1024.

Agora, o canal do socket do servidor está escutando por conexões de entrada na porta 19. Para aceitar uma, chame o método **accept()**, que retorna um objeto **SocketChannel**:

```java
SocketChannel clientChannel = serverChannel.accept();  
```  

No lado do servidor, você certamente vai querer configurar o canal do cliente como **não bloqueante (nonblocking)**, para permitir que o servidor processe múltiplas conexões simultâneas:

```java
clientChannel.configureBlocking(false);  
```

>Eu,
>Não esqueça de verificar ambos os bloking de socket server e de client, no My Torrent.
>O accept() e o clientChannel

**Você também pode querer tornar o ServerSocketChannel não bloqueante.** Por padrão, o método **accept()** bloqueia até que haja uma conexão de entrada, assim como o método **accept()** da classe **ServerSocket**. Para mudar isso, basta chamar **configureBlocking(false)** antes de chamar **accept()**:

```java
serverChannel.configureBlocking(false);  
```  

Um **accept()** não bloqueante retorna **null** quase imediatamente se não houver conexões de entrada. Certifique-se de verificar isso, ou você receberá um **NullPointerException** ao tentar usar o socket.

Agora, há dois canais abertos: um canal do servidor e um canal do cliente. Ambos precisam ser processados, e ambos podem operar indefinidamente. Além disso, o processamento do canal do servidor criará mais canais de cliente abertos. Na abordagem tradicional, você atribui uma thread para cada conexão, e o número de threads aumenta rapidamente conforme os clientes se conectam. Em vez disso, na nova API de E/S, você cria um **Selector**, que permite ao programa iterar sobre todas as conexões prontas para serem processadas. Para criar um novo **Selector**, basta chamar o método estático **Selector.open()**:

```java

Selector selector = Selector.open();  
```  

Em seguida, você precisa registrar cada canal no seletor que irá monitorá-lo, usando o método **register()** do canal. Ao registrar, especifique a operação de interesse usando uma constante da classe **SelectionKey**. Para o socket do servidor, a única operação de interesse é **OP_ACCEPT**—ou seja, o canal do servidor está pronto para aceitar uma nova conexão?

```java

serverChannel.register(selector, SelectionKey.OP_ACCEPT);  
```  

Para os canais do cliente, você quer verificar algo um pouco diferente—especificamente, se eles estão prontos para receber dados escritos. Para isso, use a chave **OP_WRITE**:

```java

SelectionKey key = clientChannel.register(selector, SelectionKey.OP_WRITE);  
```  

Ambos os métodos **register()** retornam um objeto **SelectionKey**. No entanto, você só precisará usar essa chave para os canais do cliente, pois pode haver vários deles. Cada **SelectionKey** possui um anexo (**attachment**) de tipo **Object** arbitrário, normalmente usado para armazenar um objeto que indica o estado atual da conexão. Neste caso, você pode armazenar o buffer que o canal escreve na rede. Quando o buffer estiver completamente esvaziado, você o recarrega.

Preencha um array com os dados que serão copiados para cada buffer. Em vez de escrever até o final do buffer e depois retroceder para o início para escrever novamente, é mais fácil começar com duas cópias sequenciais dos dados, de modo que cada linha esteja disponível como uma sequência contígua no array:

```java
byte[] rotation = new byte[95 * 2];  
for (byte i = ' '; i <= '~'; i++) {  
    rotation[i - ' '] = i;  
    rotation[i + 95 - ' '] = i;  
}  
```


**Como esse array só será lido após sua inicialização, você pode reutilizá-lo para múltiplos canais.** No entanto, cada canal terá seu próprio buffer preenchido com os dados desse array. Você preencherá o buffer com os primeiros 72 bytes do array `rotation`, depois adicionará um par de retorno de carro/linha nova (`\r\n`) para quebrar a linha. Em seguida, você dará um `flip()` no buffer para prepará-lo para ser esvaziado e o anexará à chave (`SelectionKey`) do canal:

```java
ByteBuffer buffer = ByteBuffer.allocate(74);  
buffer.put(rotation, 0, 72);  
buffer.put((byte) '\r');  
buffer.put((byte) '\n');  
buffer.flip();  
key2.attach(buffer);  
```  

Para verificar se algo está pronto para ser processado, chame o método `select()` do seletor. Em um servidor de longa execução, isso normalmente fica em um loop infinito:

```java
while (true) {  
    selector.select();  
    // Processa as chaves selecionadas...  
}  
```  

Se o seletor encontrar um canal pronto, seu método `selectedKeys()` retornará um `java.util.Set` contendo um objeto `SelectionKey` para cada canal pronto. Caso contrário, retornará um conjunto vazio. De qualquer forma, você pode iterar sobre ele usando um `java.util.Iterator`:

```java
Set<SelectionKey> readyKeys = selector.selectedKeys();  
Iterator<SelectionKey> iterator = readyKeys.iterator();  
while (iterator.hasNext()) {  
    SelectionKey key = iterator.next();  
    // Remove a chave do conjunto para não processá-la duas vezes  
    iterator.remove();  
    // Opera no canal...  
}  
```  

Remover a chave do conjunto informa ao `Selector` que você já a processou, e ele não precisará continuar fornecendo-a toda vez que você chamar `select()`. O `Selector` adicionará o canal de volta ao conjunto de prontos quando `select()` for chamado novamente, se o canal ficar pronto outra vez. É muito importante remover a chave do conjunto de prontos nesse momento.

Se o canal pronto for o canal do servidor, o programa aceita um novo canal de socket (`SocketChannel`) e o adiciona ao seletor. Se o canal pronto for um canal de socket, o programa escreve o máximo possível do buffer no canal. Se nenhum canal estiver pronto, o seletor aguarda até que um fique. Dessa forma, **uma única thread (a thread principal) processa múltiplas conexões simultâneas**.

Nesse caso, é fácil identificar se um canal do cliente ou do servidor foi selecionado, pois o canal do servidor só estará pronto para aceitar conexões (`OP_ACCEPT`), enquanto os canais do cliente só estarão prontos para escrita (`OP_WRITE`).

**Ambos estão prontos para operações de E/S (aceitar conexões ou escrever dados), e ambos podem lançar IOExceptions por diversos motivos.** Por isso, é importante envolver esse código em um bloco `try`:

```java
try {
    if (key.isAcceptable()) {  // Se a chave estiver pronta para aceitar conexões
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel connection = server.accept();  // Aceita uma nova conexão
        connection.configureBlocking(false);  // Configura como não bloqueante
        connection.register(selector, SelectionKey.OP_WRITE);  // Registra para escrita
        // Configura o buffer para o cliente...
    } else if (key.isWritable()) {  // Se a chave estiver pronta para escrita
        SocketChannel client = (SocketChannel) key.channel();
        // Escreve dados no cliente...
    }
}
```  

**Escrever os dados no canal é simples:** Recupere o anexo (`attachment`) da chave, converta-o para `ByteBuffer` e verifique se ainda há dados não escritos no buffer usando `hasRemaining()`. Se houver, escreva-os. Caso contrário, recarregue o buffer com a próxima linha do array `rotation` e repita o processo.

```java
ByteBuffer buffer = (ByteBuffer) key.attachment();
if (!buffer.hasRemaining()) {  // Se o buffer estiver vazio
    // Recarrega o buffer com a próxima linha
    buffer.rewind();  // Volta ao início do buffer
    int first = buffer.get();  // Lê o primeiro byte
    // Avança para o próximo caractere
    buffer.rewind();
    int position = first - ' ' + 1;  // Calcula a posição no array rotation
    buffer.put(rotation, position, 72);  // Preenche com 72 bytes
    buffer.put((byte) '\r');  // Adiciona quebra de linha (CR)
    buffer.put((byte) '\n');  // Adiciona quebra de linha (LF)
    buffer.flip();  // Prepara o buffer para leitura
}
client.write(buffer);  // Escreve os dados no canal do cliente
```  

**O algoritmo que determina a próxima linha de dados** se baseia no fato de que os caracteres estão armazenados no array `rotation` em ordem ASCII. `buffer.get()` lê o primeiro byte do buffer, e subtraindo o caractere de espaço (`' '` ou 32), obtemos o índice atual no array. Somando 1, avançamos para a próxima linha e recarregamos o buffer.

**No protocolo *chargen*, o servidor nunca fecha a conexão.** Ele espera que o cliente encerre o socket. Quando isso acontece, uma exceção é lançada. Nesse caso, cancelamos a chave (`SelectionKey`) e fechamos o canal correspondente:

```java
catch (IOException ex) {
    key.cancel();  // Cancela o registro da chave no seletor
    try {
        key.channel().close();  // Fecha o canal
    } catch (IOException cex) {
        // Ignora exceções ao fechar
    }
}
```  

**O Exemplo 11-2 reúne tudo isso em um servidor *chargen* completo**, que processa múltiplas conexões de forma eficiente usando apenas uma thread.

*(Nota: O trecho final menciona um "Exemplo 11-2", que seria a implementação completa do servidor, mas não está incluído no texto fornecido.)*

Example 11-2. A nonblocking chargen server
```java


import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.io.IOException;

public class ChargenServer { 
	public static int DEFAULT_PORT = 19;
	
	public static void main(String[] args) { 
		int port;
		
		try { port = Integer.parseInt(args[0]); } 
		catch (RuntimeException ex) { port = DEFAULT_PORT; } 
		
		System.out.println("Listening for connections on port " + port);
		byte[] rotation = new byte[95*2];
		for (byte i = ' '; i <= '~'; i++) {
			rotation[i -' '] = i;
			rotation[i + 95 - ' '] = i; 
		}
		
		ServerSocketChannel serverChannel;
		Selector selector;
		
		try { 
			serverChannel = ServerSocketChannel.open();
			ServerSocket ss = serverChannel.socket();
			InetSocketAddress address = new InetSocketAddress(port);
			ss.bind(address);
			serverChannel.configureBlocking(false);
			
			selector = Selector.open(); 
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException ex) {
			ex.printStackTrace(); 
			return; 
		}
		
		while (true) {
			try { selector.select(); } 
			catch (IOException ex) { ex.printStackTrace(); break; }
			
			Set readyKeys = selector.selectedKeys();
			
			Iterator iterator = readyKeys.iterator();
			
			while (iterator.hasNext()) { 
				SelectionKey key = iterator.next();
				iterator.remove();
				try { 
					if (key.isAcceptable()) { 
						ServerSocketChannel server = (ServerSocketChannel) key.channel(); 
						SocketChannel client = server.accept();
						System.out.println("Accepted connection from " + client);
						client.configureBlocking(false);
						SelectionKey key2 = client.register(selector, SelectionKey. OP_WRITE);
						ByteBuffer buffer = ByteBuffer.allocate(74);
						buffer.put(rotation, 0, 72);
						buffer.put((byte) '\r');
						buffer.put((byte) '\n');
						buffer.flip();
						key2.attach(buffer); 
					} else if (key.isWritable()) { 
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment(); 
						if (!buffer.hasRemaining()) { 
							// Refill the buffer with the next line
							buffer.rewind();
							// Get the old first character
							int first = buffer.get();
							// Get ready to change the data in the buffer
							buffer.rewind(); 
							// Find the new first characters position in rotation 
							int position = first - ' ' + 1;
							// copy the data from rotation into the buffer 
							buffer.put(rotation, position, 72); 
							// Store a line break at the end of the buffer 
							buffer.put((byte) '\r');
							buffer.put((byte) '\n'); 
							// Prepare the buffer for writing 
							buffer.flip(); 
						}
						
						client.write(buffer); 
					} 
				} catch (IOException ex) { 
					key.cancel();
					try { key.channel().close(); }
					catch (IOException cex) {}
				} 
			} 
		} 
	}
}


```

**Este exemplo usa apenas uma thread.** Existem situações em que você ainda pode querer usar múltiplas threads, especialmente se diferentes operações tiverem prioridades distintas. Por exemplo, talvez você queira aceitar novas conexões em uma thread de alta prioridade e atender conexões existentes em uma thread de prioridade mais baixa. No entanto, você não precisa mais manter uma proporção de 1:1 entre threads e conexões, o que melhora a escalabilidade de servidores escritos em Java.

**Também pode ser importante usar múltiplas threads para obter desempenho máximo.** Várias threads permitem que o servidor aproveite múltiplos CPUs. Mesmo com um único CPU, muitas vezes é uma boa ideia separar a thread que aceita conexões das threads de processamento. Os pools de threads discutidos no Capítulo 3 ainda são relevantes, mesmo com o novo modelo de E/S. A thread que aceita as conexões pode adicioná-las em uma fila para serem processadas pelas threads do pool.

**Isso ainda é mais eficiente do que fazer o mesmo sem seletores (selectors),** pois o método `select()` garante que você nunca perca tempo com conexões que não estão prontas para receber dados. Por outro lado, as questões de sincronização aqui são complexas, então não tente implementar isso a menos que análises de desempenho (profiling) comprovem a existência de um gargalo.


##### Buffers

No Capítulo 2, recomendei que você sempre utilize buffers em seus streams. Quase nada impacta mais o desempenho de programas de rede do que um buffer adequadamente dimensionado. No novo modelo de I/O, você não tem mais escolha - toda E/S é obrigatoriamente bufferizada. Na verdade, os buffers são elementos fundamentais dessa API.

Em vez de escrever dados em streams de saída ou ler de streams de entrada, você agora lê e escreve dados diretamente de buffers. Embora os buffers possam parecer meros arrays de bytes (como nos streams bufferizados), as implementações nativas podem conectá-los diretamente ao hardware ou memória, utilizando implementações altamente eficientes.

Do ponto de vista de programação, a principal diferença entre streams e channels é que:
- Streams são baseados em bytes - processam dados sequencialmente, um byte de cada vez (embora arrays de bytes possam ser usados para otimização)
- Channels são baseados em blocos - transferem dados em buffers completos

Antes que bytes possam ser lidos ou escritos em um channel, eles precisam estar armazenados em um buffer, sendo os dados transferidos um buffer inteiro de cada vez. Essa abordagem por blocos é fundamental para a eficiência do novo modelo de I/O.


**A segunda diferença fundamental entre streams e channels/buffers** é que os channels e buffers geralmente permitem operações de leitura e escrita no mesmo objeto. Isso nem sempre é verdade - por exemplo:

- Um channel vinculado a um arquivo em CD-ROM pode apenas ler, não escrever
- Um channel conectado a um socket com entrada desativada pode apenas escrever, não ler

Se tentar escrever em um channel somente-leitura ou ler de um channel somente-escrita, será lançada uma **UnsupportedOperationException**. Porém, em programas de rede é comum que os channels permitam ambas operações.


Podemos pensar em um buffer como uma **lista de tamanho fixo** contendo elementos de um tipo primitivo (similar a um array), embora sua implementação interna possa variar conforme o sistema operacional e hardware. Existem subclasses de **Buffer** para todos os tipos primitivos do Java (exceto boolean):

- **ByteBuffer** (usado predominantemente em redes)
- CharBuffer, ShortBuffer, IntBuffer
- LongBuffer, FloatBuffer, DoubleBuffer

Cada subclasse possui métodos com tipagem específica (ex: **DoubleBuffer** tem métodos para manipular doubles).

Todo buffer gerencia quatro informações críticas:

1. **Position** (posição):
    - Indica o próximo índice a ser lido/escrito (inicia em 0)
    - Métodos:
      ```java 
      position() // obtém 
      position(int) // define
      ```  

2. **Capacity** (capacidade):
    - Tamanho máximo do buffer (definido na criação)
    - Método:
      ```java
      capacity()
      ```  

3. **Limit** (limite):
    - Fim dos dados acessíveis (não pode ler/escrever além dele)
    - Métodos:
      ```java
      limit() // obtém 
      limit(int) // define
      ```  

4. **Mark** (marca):
    - Índice marcado pelo usuário (útil para retornar a posições específicas)
    - Métodos:
      ```java
      mark()    // define marca na posição atual  
      reset()   // retorna à marca
      ```  
   *Obs: A marca é descartada se a posição for ajustada para antes dela.*

Esses controles permitem manipulação eficiente de dados em operações de E/S não bloqueantes, especialmente em **ByteBuffer** (o mais usado em programação de redes). A ausência de generics para primitivos no Java torna necessária essa distinção por classes específicas.

Ao contrário da leitura em um **InputStream**, ler dados de um buffer **não altera** seu conteúdo. Você pode manipular a posição de leitura (avançando ou retrocedendo) e ajustar o limite para controlar até onde os dados serão lidos. Apenas a capacidade é imutável.

**Métodos Principais da Classe Buffer**

1. **clear()** - "Esvazia" o buffer:
    - Define posição = 0
    - Define limite = capacidade
    - *Obs: Não apaga os dados!* (eles permanecem acessíveis via métodos absolutos ou ajustes de posição/limite)
   ```java
   public final Buffer clear()
   ```

2. **rewind()** - Rebobina o buffer:
    - Reposiciona para posição = 0 (mantém o limite atual)
    - Útil para reler dados
   ```java
   public final Buffer rewind()
   ```

3. **flip()** - Prepara para drenagem:
    - Define limite = posição atual
    - Reposiciona para posição = 0
    - Usado após preencher o buffer e antes de escrever seus dados
   ```java
   public final Buffer flip()
   ```

**Métodos de Consulta (não alteram estado)**

- **remaining()** → Retorna elementos disponíveis entre posição e limite:
  ```java
  public final int remaining()
  ```

- **hasRemaining()** → Verifica se há elementos restantes:
  ```java
  public final boolean hasRemaining() // true se remaining() > 0
  ```

**Comparação Visual**

| Operação       | InputStream Tradicional       | Buffer NIO                  |
|----------------|-------------------------------|-----------------------------|
| Leitura        | Consome dados                 | Apenas avança posição       |
| Releitura      | Requer novo stream            | rewind() ou ajuste manual   |
| Controle       | Sequencial (sem retrocesso)   | Acesso randômico via posição|

##### Creating Buffers

**A hierarquia de classes de buffer** é baseada em herança, mas não exatamente em polimorfismo — pelo menos não no nível superior. Normalmente, você precisa saber se está lidando com um **IntBuffer**, **ByteBuffer**, **CharBuffer** ou outro tipo específico. O código é escrito para essas subclasses, não para a superclasse genérica **Buffer**.

Cada classe de buffer tipada possui vários **métodos de fábrica** que criam subclasses específicas da implementação, de diferentes formas:

- **Buffers vazios** são criados usando métodos `allocate()` (úteis para **entrada** de dados).
- **Buffers pré-preenchidos** são criados usando métodos `wrap()` (usados principalmente para **saída**).

**Exemplos:**
```java
// Aloca um ByteBuffer vazio com capacidade para 1024 bytes
ByteBuffer bufferVazio = ByteBuffer.allocate(1024);

// Cria um IntBuffer a partir de um array existente (pré-preenchido)
int[] dados = {1, 2, 3, 4};
IntBuffer bufferPreenchido = IntBuffer.wrap(dados);
```

**Pontos-chave:**
1. **Alocação (`allocate`)** → Para buffers de entrada (dados a serem preenchidos posteriormente).
2. **Empacotamento (`wrap`)** → Para buffers de saída (dados já existentes, como arrays).
3. **Especificidade de tipo** → O código opera diretamente nas subclasses (ex: `ByteBuffer`), não no `Buffer` genérico.

Essa abordagem permite otimizações específicas por tipo (ex: `ByteBuffer` para E/S de rede) e controle preciso sobre alocação de memória.

O método básico `allocate()` simplesmente retorna um novo buffer vazio com uma capacidade fixa especificada. Por exemplo, estas linhas criam buffers de byte e int, cada um com tamanho 100:

```java
ByteBuffer buffer1 = ByteBuffer.allocate(100);
IntBuffer buffer2 = IntBuffer.allocate(100);
```

O cursor é posicionado no início do buffer (ou seja, a posição é 0). Um buffer criado por `allocate()` será implementado sobre um array Java, que pode ser acessado pelos métodos `array()` e `arrayOffset()`. Por exemplo, você poderia ler um grande bloco de dados em um buffer usando um canal e depois recuperar o array do buffer para passar para outros métodos:

```java
byte[] data1 = buffer1.array();
int[] data2 = buffer2.array();
```

O método `array()` realmente expõe os dados privados do buffer, então use-o com cautela. Alterações no array de suporte são refletidas no buffer e vice-versa. O padrão normal aqui é preencher o buffer com dados, recuperar seu array de suporte e então operar no array. Isso não é um problema, desde que você não escreva no buffer depois de começar a trabalhar com o array.

A classe **ByteBuffer** (mas não as outras classes de buffer) possui um método adicional **allocateDirect()** que pode não criar um array de suporte para o buffer. A JVM pode implementar um ByteBuffer alocado diretamente usando acesso direto à memória do buffer em uma placa de rede, memória do kernel ou algo similar. Isso não é obrigatório, mas é permitido — e pode melhorar o desempenho em operações de I/O.

**Uso do `allocateDirect()`**

Do ponto de vista da API, o `allocateDirect()` funciona exatamente como o `allocate()`:
```java
ByteBuffer buffer = ByteBuffer.allocateDirect(100);
```  

**Limitações de Buffers Diretos**
- Chamar **array()** ou **arrayOffset()** em um buffer direto lança uma **UnsupportedOperationException** (pois ele pode não ter um array de suporte).
- Buffers diretos podem ser mais rápidos em algumas VMs, especialmente para buffers grandes (cerca de 1MB ou mais).
- No entanto, sua criação é mais custosa que buffers convencionais, então devem ser usados apenas quando o buffer for mantido por um tempo prolongado.

**Considerações de Desempenho**

Os detalhes variam conforme a implementação da JVM. Como regra geral:
> **Não use buffers diretos a menos que testes de desempenho comprovem que há um gargalo.**

Essa otimização é específica para casos extremos (ex: manipulação de grandes volumes de dados em redes de alta velocidade).

Se você já possui um array de dados que deseja enviar como saída, geralmente é mais eficiente **envolver** ( *wrap* ) um buffer em torno dele, em vez de alocar um novo buffer e copiar os elementos um por um. Por exemplo:

```java
byte[] data = "Alguns dados".getBytes("UTF-8");
ByteBuffer buffer1 = ByteBuffer.wrap(data);

char[] text = "Algum texto".toCharArray();
CharBuffer buffer2 = CharBuffer.wrap(text);
```  

**Características importantes:**
1. **Referência ao array original**
    - O buffer criado por `wrap()` usa o array como base (*backing array*).
2. **Alterações são refletidas**
    - Mudanças no array afetam o buffer, e vice-versa.
3. **Buffers não diretos**
    - Buffers criados com `wrap()` nunca são *direct buffers*.
4. **Momento ideal para uso**
    - Só envolva o array em um buffer quando não precisar mais modificá-lo diretamente.

Isso é útil para evitar cópias desnecessárias de dados, melhorando desempenho em operações de I/O.


##### Filling and Draining

Os buffers trabalham como uma **fila sequencial** de dados. Eles têm um ponteiro (chamado `position`) que marca onde você está lendo ou escrevendo.

###### Exemplo Prático:
1. **Preenchendo o buffer (escrita)**:
   ```java
   CharBuffer buffer = CharBuffer.allocate(12); // Buffer vazio para 12 caracteres
   buffer.put('H'); // Posição avança para 1
   buffer.put('e'); // Posição vai para 2
   buffer.put('l'); // Posição 3
   buffer.put('l'); // Posição 4
   buffer.put('o'); // Posição 5
   ```
    - Agora o buffer tem `position=5` e contém `"Hello"`.
    - Se tentar escrever além da capacidade (12), ocorre um erro (`BufferOverflowException`).

2. **Lendo o buffer (após flip)**:
   Antes de ler, é preciso ajustar o buffer:
   ```java
   buffer.flip(); // Prepara para leitura:
                  // - Limite = posição atual (5)
                  // - Posição volta para 0
   ```
   Agora podemos ler:
   ```java
   String resultado = "";
   while (buffer.hasRemaining()) { // Enquanto houver dados
       resultado += buffer.get();  // Lê e avança a posição
   }
   // Resultado final: "Hello"
   ```

###### Modo Alternativo (acesso direto):
Você pode ler/escrever em posições específicas **sem mexer** no ponteiro:
```java
CharBuffer buffer = CharBuffer.allocate(12);
buffer.put(0, 'H'); // Escreve na posição 0
buffer.put(1, 'e'); // Posição 1
// ... (ordem não importa!)
buffer.put(4, 'o'); 
```
- Vantagem: Não precisa do `flip()` antes de ler.
- Cuidado: Tentar acessar posições inválidas causa `IndexOutOfBoundsException`.

###### Comparação:
| Operação       | Sequencial (put/get) | Direto (put(index, char)) |
|---------------|----------------------|--------------------------|
| Move ponteiro? | Sim                  | Não                       |
| Precisa flip?  | Sim                  | Não                       |
| Ordem importa? | Sim                  | Não                       |

Essa estrutura torna buffers ideais para operações de E/S eficientes, especialmente em redes!


##### Bulk Methods

Mesmo usando buffers, processar **blocos de dados** (arrays) é mais rápido do que manipular um elemento por vez. Todas as classes de buffer oferecem métodos para operações em bloco.

---

###### **Exemplo com ByteBuffer**:
```java
// Métodos para LEITURA em bloco (get)
public ByteBuffer get(byte[] destino)  // Lê dados para um array completo
public ByteBuffer get(byte[] destino, int inicio, int tamanho)  // Lê para parte do array

// Métodos para ESCRITA em bloco (put)
public ByteBuffer put(byte[] origem)  // Escreve um array completo
public ByteBuffer put(byte[] origem, int inicio, int tamanho)  // Escreve parte do array
```

---

###### **Como Funciona**:
1. **Posicionamento**:
    - Os dados são lidos/escritos a partir da **posição atual** do buffer.
    - Após a operação, a posição avança automaticamente pelo tamanho do bloco.

2. **Tratamento de Erros**:
    - Se o buffer **não tiver espaço suficiente** durante uma escrita:  
      → `BufferOverflowException` (exceção não verificada).
    - Se o buffer **não tiver dados suficientes** durante uma leitura:  
      → `BufferUnderflowException` (exceção não verificada).

---

###### **Exemplo Prático**:
```java
ByteBuffer buffer = ByteBuffer.allocate(100);

// Escreve um array de bytes no buffer
byte[] dados = "Dados rápidos".getBytes();
buffer.put(dados);  // Posição avança automaticamente

// Lê os dados para outro array
buffer.flip();  // Prepara para leitura
byte[] copia = new byte[5];
buffer.get(copia, 0, 5);  // Lê os 5 primeiros bytes
```

---

###### **Por que Usar?**
- **Eficiência**: Processar blocos reduz operações individuais, melhorando desempenho.
- **Simplicidade**: Evita loops manuais para ler/escrever cada elemento.
- **Controle**: Permite trabalhar com subarrays (partes específicas de um array).


##### Data Conversion

**Todos os dados em Java são representados por bytes**  
Seja um número inteiro (`int`), decimal (`double`, `float`) ou qualquer outro tipo primitivo, tudo pode ser convertido em bytes. O inverso também é verdade: uma sequência de bytes pode ser interpretada como um dado primitivo. Por exemplo:
- 4 bytes podem virar um `int` ou `float` (dependendo da interpretação)
- 8 bytes podem virar um `long` ou `double`

---

###### **Métodos do ByteBuffer**

A classe `ByteBuffer` tem métodos especiais para **ler** (`get`) e **escrever** (`put`) tipos primitivos diretamente como bytes:

###### **Métodos Básicos (posição relativa)**
```java
buffer.putInt(42);       // Escreve um int (4 bytes)  
int valor = buffer.getInt();  // Lê um int  

buffer.putDouble(3.14);  // Escreve um double (8 bytes)  
double pi = buffer.getDouble();  
```
*(Disponível para todos os tipos primitivos, exceto `boolean`)*

###### **Métodos com Posição Fixa**
```java
buffer.putInt(10, 42);   // Escreve um int na posição 10  
int valor = buffer.getInt(10);  // Lê o int da posição 10  
```
---

###### **Controle de Endianness**

Você pode escolher a ordem dos bytes (*big-endian* ou *little-endian*):
```java
// Verifica a ordem atual (padrão: BIG_ENDIAN)  
if (buffer.order().equals(ByteOrder.BIG_ENDIAN)) {  
    buffer.order(ByteOrder.LITTLE_ENDIAN); // Muda para little-endian  
}  
```
- **Big-endian**: Byte mais significativo primeiro (padrão do Java)
- **Little-endian**: Byte menos significativo primeiro (usado em alguns sistemas)

---

###### **Comparação com I/O Tradicional**

Esses métodos substituem o que faziam `DataOutputStream` e `DataInputStream`, mas com vantagens:
1. **Flexibilidade**: Escolha entre big-endian e little-endian.
2. **Eficiência**: Trabalho direto com buffers, sem conversões extras.

**Exemplo Prático**:
```java
ByteBuffer buffer = ByteBuffer.allocate(16);  
buffer.putInt(100);  
buffer.putDouble(3.14);  

buffer.flip(); // Prepara para leitura  
int num = buffer.getInt();  
double pi = buffer.getDouble();  
```  

Em vez de usar um protocolo de carregamento (chargen), você pode testar a rede gerando dados binários. Esse teste pode revelar problemas que não são visíveis no protocolo ASCII, como:

- Um *gateway* antigo configurado para remover o bit mais significativo de cada byte.
- Descartar a cada 230º byte.
- Entrar em modo de diagnóstico devido a uma sequência inesperada de caracteres de controle.

Esses não são problemas teóricos—já vi variações deles acontecerem.

Uma forma de testar a rede é enviar todos os *integers* possíveis (de 0 a ~4,3 bilhões), verificando todas as combinações de 4 bytes. No receptor, basta comparar se os dados recebidos são os esperados. Se houver erros, é fácil identificar onde ocorreram.

###### Funcionamento do protocolo (chamado de **Intgen**):
1. O cliente se conecta ao servidor.
2. O servidor começa a enviar *integers* de 4 bytes (formato *big-endian*), iniciando em 0 e incrementando +1 a cada envio. Eventualmente, os números ficarão negativos (ao ultrapassar 2³¹).
3. O servidor roda indefinidamente; o cliente fecha a conexão quando quiser.

###### Implementação:
O servidor armazena o *integer* atual em um **ByteBuffer** de 4 bytes (um buffer por canal). Quando o canal está pronto para escrita:
1. Os dados do buffer são enviados.
2. O buffer é retrocedido (*rewind*), lido com `getInt()`, e então limpo.
3. O valor é incrementado +1, salvo no buffer com `putInt()`, e preparado para o próximo envio (*flip*).
4.
```java
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.io.IOException;

public class IntgenServer {
    public static int DEFAULT_PORT = 1919;

    public static void main(String[] args) {
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (RuntimeException ex) {
            port = DEFAULT_PORT;
        }

        System.out.println("Escutando conexões na porta " + port);

        ServerSocketChannel serverChannel;
        Selector selector;

        try {
            serverChannel = ServerSocketChannel.open();
            ServerSocket ss = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(port);
            ss.bind(address);
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        while (true) {
            try {
                selector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        System.out.println("Conexão aceita de " + client);
                        client.configureBlocking(false);
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE);
                        ByteBuffer output = ByteBuffer.allocate(4);
                        output.putInt(0);
                        output.flip();
                        key2.attach(output);
                    } else if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = (ByteBuffer) key.attachment();
                        if (!output.hasRemaining()) {
                            output.rewind();
                            int value = output.getInt();
                            output.clear();
                            output.putInt(value + 1);
                            output.flip();
                        }
                        client.write(output);
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {}
                }
            }
        }
    }
}
```

###### **Explicação Simplificada:**
Este código implementa um **servidor que envia sequências numéricas (inteiros de 4 bytes) para clientes conectados**.

1. **Configuração do Servidor:**
    - Escuta na porta **1919** (ou outra definida pelo usuário).
    - Usa **NIO (Non-blocking I/O)** para lidar com múltiplas conexões sem bloquear a thread principal.

2. **Funcionamento:**
    - Quando um cliente se conecta, o servidor começa a enviar números inteiros, começando em **0** e incrementando **+1** a cada envio.
    - Os dados são enviados em um **ByteBuffer** (buffer binário) no formato *big-endian*.
    - Se o buffer estiver vazio, ele é recarregado com o próximo número.

3. **Tratamento de Erros:**
    - Se ocorrer um erro de E/S (IOException), a conexão é fechada.

Este servidor roda **indefinidamente**, e o cliente pode desconectar quando desejar.
##### View Buffers

Se você sabe que o **ByteBuffer** lido de um **SocketChannel** contém apenas elementos de um tipo primitivo específico (como `int`, `double`, etc.), pode ser útil criar um **buffer de visualização** (*view buffer*).

Esse buffer é um novo objeto **Buffer** do tipo adequado (por exemplo, `IntBuffer`, `DoubleBuffer`, etc.), que obtém seus dados do **ByteBuffer** subjacente, começando na posição atual.

- **Alterações no buffer de visualização** refletem no **ByteBuffer** original, e vice-versa.
- Porém, cada buffer tem seu próprio **limite**, **capacidade**, **marca** e **posição**, independentes.

Os buffers de visualização são criados usando um destes métodos do **ByteBuffer**:
```java
public abstract ShortBuffer asShortBuffer()  
public abstract CharBuffer asCharBuffer()  
public abstract IntBuffer asIntBuffer()  
public abstract LongBuffer asLongBuffer()  
public abstract FloatBuffer asFloatBuffer()  
public abstract DoubleBuffer asDoubleBuffer()  
```  

###### Exemplo Prático:
No caso de um **cliente para o protocolo Intgen** (que só lê inteiros), seria mais eficiente usar um **IntBuffer** em vez de manipular diretamente um **ByteBuffer**.

```java
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.IOException;

public class ClienteIntgen {
    public static int PORTA_PADRAO = 1919;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java ClienteIntgen host [porta]");
            return;
        }

        int porta;
        try {
            porta = Integer.parseInt(args[1]);
        } catch (RuntimeException ex) {
            porta = PORTA_PADRAO;
        }

        try {
            SocketAddress endereco = new InetSocketAddress(args[0], porta);
            SocketChannel cliente = SocketChannel.open(endereco);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            IntBuffer view = buffer.asIntBuffer();

            for (int esperado = 0; ; esperado++) {
                cliente.read(buffer);
                int atual = view.get();
                buffer.clear();
                view.rewind();

                if (atual != esperado) {
                    System.err.println("Esperado " + esperado + "; recebido " + atual);
                    break;
                }
                System.out.println(atual);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}
```

###### Observações Importantes:

1. **Uso dos Buffers**
    - Embora você possa preencher e esvaziar os buffers usando apenas métodos da classe `IntBuffer`,
    - A leitura/escrita no canal deve ser feita usando o `ByteBuffer` original, pois `SocketChannel` só trabalha com `ByteBuffer`.

2. **Gerenciamento de Buffer**
    - O `ByteBuffer` deve ser limpo (`clear()`) a cada iteração para evitar estouro.
    - As posições e limites dos buffers (`ByteBuffer` e `IntBuffer`) são independentes e devem ser gerenciados separadamente.

3. **Modo Não-Bloqueante (NIO)**
    - Em modo não-bloqueante, não há garantia de que os dados estarão alinhados corretamente (ex.: um `int` pode ser escrito parcialmente).
    - Sempre verifique se o `ByteBuffer` subjacente foi totalmente preenchido/drenado antes de acessar o buffer de visualização (`IntBuffer`).

**Código com Problema (Não limpa o ByteBuffer)**

```java

import java.nio.*;
import java.nio.channels.*;
import java.net.*;

public class ProblemaBufferNaoLimpado {
    public static void main(String[] args) throws Exception {
        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 1919));
        ByteBuffer buffer = ByteBuffer.allocate(4);
        IntBuffer intBuffer = buffer.asIntBuffer();

        while (true) {
            client.read(buffer); // Lê dados do servidor (Intgen)
            int valor = intBuffer.get(); // Pega o valor do IntBuffer
            System.out.println("Recebido: " + valor);

            // **ESQUECEU DE LIMPAR O BUFFER!**
            // buffer.clear();  // <-- Faltou esta linha!
            // intBuffer.rewind();

            // Resultado: O programa trava após a primeira leitura,
            // porque o buffer fica cheio e não há espaço para novos dados.
        }
    }
}

```

**Solução Correta (Limpando o Buffer)**

```java

while (true) {
    client.read(buffer);
    int valor = intBuffer.get();
    System.out.println("Recebido: " + valor);

    // Limpa o ByteBuffer e reseta o IntBuffer para a próxima leitura
    buffer.clear();
    intBuffer.rewind();
}

```

##### Compacting Buffers

A maioria dos buffers graváveis suporta um método `compact()`:

```java
public abstract ByteBuffer compact()
public abstract IntBuffer compact()
public abstract ShortBuffer compact()
public abstract FloatBuffer compact()
public abstract CharBuffer compact()
public abstract DoubleBuffer compact()
```

(Se não fosse pelo encadeamento de invocações, esses seis métodos poderiam ter sido substituídos por um único método na superclasse `Buffer` comum.)

Compactar desloca quaisquer dados restantes no buffer para o início do buffer, liberando mais espaço para novos elementos. Quaisquer dados que estavam nessas posições serão sobrescritos. A posição (`position`) do buffer é definida para o final dos dados restantes, ficando pronta para receber mais dados.

A compactação é uma operação especialmente útil quando você está copiando dados - lendo de um canal e escrevendo em outro usando E/S não bloqueante. Você pode ler alguns dados para um buffer, escrever o buffer novamente e depois compactar os dados, de modo que todos os dados não escritos fiquem no início do buffer, e a posição fique no final dos dados restantes no buffer, pronta para receber mais dados.

Isso permite que as leituras e escritas sejam intercaladas de forma mais ou menos aleatória com apenas um buffer. Várias leituras podem ocorrer em sequência, ou várias escritas consecutivas. Se a rede estiver pronta para saída imediata, mas não para entrada (ou vice-versa), o programa pode aproveitar isso.

Esta técnica pode ser usada para implementar um servidor de eco, como mostrado no Exemplo. O protocolo de eco simplesmente responde ao cliente com os mesmos dados que o cliente enviou. Assim como o `chargen`, é útil para testes de rede. Também como o `chargen`, o eco depende do cliente para fechar a conexão. No entanto, diferentemente do `chargen`, um servidor de eco deve tanto ler quanto escrever na conexão.


Servidor Echo
```java
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.io.IOException;

public class ServidorEcho {
    public static int PORTA_PADRAO = 7;
    
    public static void main(String[] args) {
        int porta;
        try {
            porta = Integer.parseInt(args[0]);
        } catch (RuntimeException ex) {
            porta = PORTA_PADRAO;
        }
        
        System.out.println("Aguardando conexões na porta " + porta);
        
        ServerSocketChannel canalServidor;
        Selector seletor;
        
        try {
            canalServidor = ServerSocketChannel.open();
            ServerSocket socket = canalServidor.socket();
            InetSocketAddress endereco = new InetSocketAddress(porta);
            socket.bind(endereco);
            canalServidor.configureBlocking(false);
            seletor = Selector.open();
            canalServidor.register(seletor, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        
        while (true) {
            try {
                seletor.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
            
            Set<SelectionKey> chavesProntas = seletor.selectedKeys();
            Iterator<SelectionKey> iterador = chavesProntas.iterator();
            
            while (iterador.hasNext()) {
                SelectionKey chave = iterador.next();
                iterador.remove();
                
                try {
                    if (chave.isAcceptable()) {
                        ServerSocketChannel servidor = (ServerSocketChannel) chave.channel();
                        SocketChannel cliente = servidor.accept();
                        System.out.println("Conexão aceita de " + cliente);
                        cliente.configureBlocking(false);
                        SelectionKey chaveCliente = cliente.register(
                            seletor, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                        ByteBuffer buffer = ByteBuffer.allocate(100);
                        chaveCliente.attach(buffer);
                    }
                    
                    if (chave.isReadable()) {
                        SocketChannel cliente = (SocketChannel) chave.channel();
                        ByteBuffer saida = (ByteBuffer) chave.attachment();
                        cliente.read(saida);
                    }
                    
                    if (chave.isWritable()) {
                        SocketChannel cliente = (SocketChannel) chave.channel();
                        ByteBuffer saida = (ByteBuffer) chave.attachment();
                        saida.flip();
                        cliente.write(saida);
                        saida.compact();
                    }
                } catch (IOException ex) {
                    chave.cancel();
                    try {
                        chave.channel().close();
                    } catch (IOException cex) {}
                }
            }
        }
    }
}
```



Algo que notei ao escrever e depurar este programa: o tamanho do buffer faz uma grande diferença, embora talvez não da maneira que você poderia imaginar.

1. **Buffers grandes podem esconder erros**:
    - Se o buffer for grande o suficiente para armazenar casos de teste completos sem precisar ser invertido (`flip`) ou esvaziado, é fácil não perceber que o buffer não está sendo manipulado nos momentos certos.
    - Os casos de teste podem passar sem revelar problemas porque nunca exigem essas operações.

2. **Teste com buffers pequenos**:
    - Antes de distribuir seu programa, teste com tamanhos de buffer significativamente menores que o esperado.
    - Neste caso, testei com um buffer de apenas 10 bytes.
    - Isso reduz o desempenho, então não use buffers tão pequenos em produção, mas é essencial para garantir que o código se comporte corretamente quando o buffer encher.

3. **Objetivo do teste**:
    - Verificar se o programa lida corretamente com operações de buffer quando ele atinge sua capacidade.
    - Garantir que todas as operações de flip e compactação estão sendo feitas nos momentos adequados.

##### Duplicating Buffers

É comum querermos fazer uma cópia de um buffer para enviar a mesma informação a dois ou mais canais. Os métodos `duplicate()` em cada uma das seis classes de buffer tipadas fazem isso:

```java
public abstract ByteBuffer duplicate()
public abstract IntBuffer duplicate()
public abstract ShortBuffer duplicate()
public abstract FloatBuffer duplicate()
public abstract CharBuffer duplicate()
public abstract DoubleBuffer duplicate()
```

**Importante sobre essas cópias:**
1. **Não são clones independentes**
    - Os buffers duplicados compartilham os mesmos dados (incluindo o mesmo array subjacente, se o buffer for indireto)
    - Alterações em um buffer são refletidas no outro

2. **Melhor uso**
    - Ideal para operações de **leitura**
    - Para escrita, pode ser difícil rastrear onde os dados estão sendo modificados

3. **Independência parcial**
    - Marcas (`marks`), limites (`limits`) e posições (`positions`) são independentes
    - Um buffer pode estar "adiantado" ou "atrasado" em relação ao outro na leitura/escrita

---

###### Exemplo Prático:
Se você duplicar um buffer que contém `[A, B, C, D]`:
```java
ByteBuffer original = ByteBuffer.wrap(new byte[]{'A', 'B', 'C', 'D'});
ByteBuffer copia = original.duplicate();

original.get(); // Avança a posição do original (agora aponta para 'B')
System.out.println((char) copia.get()); // A cópia ainda lê 'A' (posição independente)
```

---

###### Quando Usar:
- **Cenário útil**: Enviar os mesmos dados para múltiplos sockets NIO
- **Cuidado**: Se um canal modificar o buffer (ex.: `put()`), a alteração afeta ambos!

A duplicação é útil quando você deseja transmitir os mesmos dados por múltiplos canais de forma aproximadamente paralela. Você pode criar duplicatas do buffer principal para cada canal e permitir que cada um opere em sua própria velocidade.

Por exemplo, lembre-se do servidor HTTP de arquivo único no Exemplo 9-10. Quando reimplementado com canais e buffers, como mostrado no Exemplo 11-6 (NonblockingSingleFileHTTPServer), o arquivo único a ser servido fica armazenado em um buffer constante e somente leitura. Toda vez que um cliente se conecta, o programa cria uma duplicata desse buffer específica para aquele canal, que é armazenada como anexo (attachment) do canal.

Sem as duplicatas, um cliente teria que esperar até que o outro terminasse para que o buffer original pudesse ser rebobinado (rewound). As duplicatas permitem a reutilização simultânea do buffer.


```java
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.net.*;

public class ServidorHTTPArquivoUnicoNaoBloqueante {
    private ByteBuffer bufferConteudo;
    private int porta = 80;

    public ServidorHTTPArquivoUnicoNaoBloqueante(
        ByteBuffer dados, String codificacao, String tipoMIME, int porta) {
        
        this.porta = porta;
        String cabecalho = "HTTP/1.0 200 OK\r\n"
            + "Server: ServidorHTTPArquivoUnicoNaoBloqueante\r\n"
            + "Content-length: " + dados.limit() + "\r\n"
            + "Content-type: " + tipoMIME + "\r\n\r\n";
        
        byte[] dadosCabecalho = cabecalho.getBytes(Charset.forName("US-ASCII"));
        ByteBuffer buffer = ByteBuffer.allocate(dados.limit() + dadosCabecalho.length);
        buffer.put(dadosCabecalho);
        buffer.put(dados);
        buffer.flip();
        this.bufferConteudo = buffer;
    }

    public void executar() throws IOException {
        ServerSocketChannel canalServidor = ServerSocketChannel.open();
        ServerSocket socketServidor = canalServidor.socket();
        Selector seletor = Selector.open();
        InetSocketAddress portaLocal = new InetSocketAddress(porta);
        socketServidor.bind(portaLocal);
        canalServidor.configureBlocking(false);
        canalServidor.register(seletor, SelectionKey.OP_ACCEPT);
        
        while (true) {
            seletor.select();
            Iterator<SelectionKey> chaves = seletor.selectedKeys().iterator();
            
            while (chaves.hasNext()) {
                SelectionKey chave = chaves.next();
                chaves.remove();
                
                try {
                    if (chave.isAcceptable()) {
                        ServerSocketChannel servidor = (ServerSocketChannel) chave.channel();
                        SocketChannel canal = servidor.accept();
                        canal.configureBlocking(false);
                        canal.register(seletor, SelectionKey.OP_READ);
                        
                    } else if (chave.isWritable()) {
                        SocketChannel canal = (SocketChannel) chave.channel();
                        ByteBuffer buffer = (ByteBuffer) chave.attachment();
                        
                        if (buffer.hasRemaining()) {
                            canal.write(buffer);
                        } else { // Concluído
                            canal.close();
                        }
                        
                    } else if (chave.isReadable()) {
                        // Não analisa o cabeçalho HTTP, apenas lê algo
                        SocketChannel canal = (SocketChannel) chave.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(4096);
                        canal.read(buffer);
                        // Muda o canal para modo somente escrita
                        chave.interestOps(SelectionKey.OP_WRITE);
                        chave.attach(bufferConteudo.duplicate());
                    }
                } catch (IOException ex) {
                    chave.cancel();
                    try {
                        chave.channel().close();
                    } catch (IOException cex) {}
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(
                "Uso: java ServidorHTTPArquivoUnicoNaoBloqueante arquivo porta codificacao");
            return;
        }
        
        try {
            // Lê o arquivo único a ser servido
            String tipoConteudo = URLConnection.getFileNameMap().getContentTypeFor(args[0]);
            Path arquivo = FileSystems.getDefault().getPath(args[0]);
            byte[] dados = Files.readAllBytes(arquivo);
            ByteBuffer entrada = ByteBuffer.wrap(dados);
            
            // Define a porta para escutar
            int porta;
            try {
                porta = Integer.parseInt(args[1]);
                if (porta < 1 || porta > 65535) porta = 80;
            } catch (RuntimeException ex) {
                porta = 80;
            }
            
            String codificacao = "UTF-8";
            if (args.length > 2) codificacao = args[2];
            
            ServidorHTTPArquivoUnicoNaoBloqueante servidor =
                new ServidorHTTPArquivoUnicoNaoBloqueante(
                    entrada, codificacao, tipoConteudo, porta);
            servidor.executar();
            
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
```

###### Explicação do Funcionamento:

1. **Configuração Inicial**:
    - O construtor prepara os dados a serem enviados junto com um cabeçalho HTTP contendo informações sobre tamanho e tipo do conteúdo
    - O cabeçalho e o corpo da resposta são armazenados em um único ByteBuffer para envio rápido

2. **Conexões Paralelas**:
    - Clientes diferentes podem estar em posições diferentes do arquivo
    - Cada canal recebe sua própria cópia do buffer (via `duplicate()`)
    - Todos compartilham o mesmo conteúdo, mas com índices independentes

3. **Gerenciamento de Conexões**:
    - Todas as conexões são tratadas por um único Selector no método `executar()`
    - Configuração inicial similar ao servidor chargen anterior
    - Quando um SocketChannel é aceito, é registrado para leitura inicial (protocolo HTTP exige requisição do cliente primeiro)

4. **Fluxo de Operação**:
    - Aceita conexões (OP_ACCEPT)
    - Lê requisições (OP_READ)
    - Escreve respostas (OP_WRITE)
    - Usa buffers duplicados para cada cliente


A resposta a uma leitura é simplificada. O programa lê o máximo de bytes de entrada possível (até 4KB) e então redefine as operações de interesse do canal para escrita. (Um servidor mais completo analisaria o cabeçalho da requisição HTTP aqui e escolheria o arquivo para enviar com base nessas informações.) Em seguida, o buffer de conteúdo é duplicado e associado ao canal.

Na próxima passagem pelo loop while, esse canal deverá estar pronto para receber dados (ou, se não dessa vez, na próxima - a natureza assíncrona da conexão significa que só a veremos quando estiver pronta). Nesse ponto, o buffer é recuperado do anexo e o máximo possível de dados é escrito no canal. Não há problema se não for possível escrever tudo de uma vez - o processo continuará de onde parou na próxima iteração. O buffer mantém automaticamente o controle de sua própria posição.

Embora muitos clientes conectados possam resultar na criação de vários objetos buffer, o overhead real é mínimo porque todos compartilham os mesmos dados subjacentes.

O método main() lê parâmetros da linha de comando:
1. O nome do arquivo a ser servido vem do primeiro argumento
2. Se nenhum arquivo for especificado ou não puder ser aberto, exibe mensagem de erro e encerra
3. Se o arquivo puder ser lido, seu conteúdo é carregado em um ByteBuffer usando as classes Path e Files do Java 7
4. O tipo de conteúdo do arquivo é estimado e armazenado na variável contentType
5. A porta é lida do segundo argumento (usa porta 80 se não especificada ou inválida)
6. A codificação é lida do terceiro argumento (assume UTF-8 se não presente)
7. Esses valores são usados para criar um objeto NonblockingSingleFileHTTPServer e iniciá-lo


##### Slicing Buffers

**Fatiar (slicing) um buffer** é uma variação da duplicação. Assim como a duplicação, ele cria um novo buffer que compartilha dados com o buffer original. Porém, há diferenças importantes:

1. **Posição Inicial**
    - A posição zero do buffer fatiado corresponde à posição atual do buffer original quando o slice foi criado.
    - Sua capacidade vai apenas até o limite (limit) do buffer original.

2. **Funcionamento Prático**
    - O buffer fatiado é como uma "janela" que mostra apenas os dados entre a posição atual e o limite do buffer original no momento do fatiamento.
    - Se você rebobinar (rewind) o buffer fatiado, ele voltará à posição inicial do fatiamento – não consegue acessar dados anteriores no buffer original.

3. **Métodos Disponíveis**  
   Cada classe de buffer tipado possui seu próprio método `slice()`:
   ```java
   public abstract ByteBuffer slice()
   public abstract IntBuffer slice()
   public abstract ShortBuffer slice()
   public abstract FloatBuffer slice()
   public abstract CharBuffer slice()
   public abstract DoubleBuffer()
   ```

###### Caso de Uso Prático:
É ideal quando você tem um buffer grande com partes lógicas distintas – como um **cabeçalho de protocolo seguido de dados**. Você pode:
1. Ler o cabeçalho primeiro
2. Fatiar o buffer para criar uma nova visão contendo **apenas os dados**
3. Passar esse buffer fatiado para outro método/classe

Exemplo:
```java
ByteBuffer bufferOriginal = ByteBuffer.wrap(new byte[]{'H', 'E', 'A', 'D', 'D', 'A', 'T', 'A'});
bufferOriginal.position(4); // Pula o cabeçalho "HEAD"
ByteBuffer bufferDados = bufferOriginal.slice(); // Contém apenas "DATA"
```

###### Diferença Chave vs. Duplicação:
- **Duplicação (`duplicate()`)**  
  Copia todo o buffer, mantendo posição/limite independentes, mas com acesso a todos os dados originais.

- **Fatiamento (`slice()`)**  
  Cria uma visão restrita a uma subseção do buffer original, ignorando dados anteriores.


##### Marking and Resetting

Assim como os fluxos de entrada (*input streams*), os buffers podem ser marcados (*marked*) e resetados (*reset*) se você precisar reler alguns dados. No entanto, diferentemente dos fluxos de entrada, isso pode ser feito em **todos os buffers**, não apenas em alguns deles.

Os métodos relevantes são declarados uma única vez na superclasse `Buffer` e herdados por todas as subclasses:

```java
public final Buffer mark()  
public final Buffer reset()  
```

**Comportamento e Exceções:**
- O método `reset()` lança uma `InvalidMarkException` (uma exceção em tempo de execução) se a marca (*mark*) não tiver sido definida.
- A marca também é **removida automaticamente** quando a posição (*position*) é ajustada para um ponto anterior à marca.

---

###### Exemplo de Uso:
```java
ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1, 2, 3, 4});
buffer.position(1); // Posiciona no segundo byte
buffer.mark();      // Define uma marca na posição atual (índice 1)
buffer.position(3); // Avança para o quarto byte
buffer.reset();     // Volta para a marca (índice 1)
```

---

###### Pontos Importantes:
1. **Marcação Flexível**:
    - Útil para reler dados sem recarregar o buffer.
    - Funciona em qualquer tipo de buffer (`ByteBuffer`, `IntBuffer`, etc.).

2. **Cuidados com a Marca**:
    - Se você mover a posição para **antes da marca** (ex.: `buffer.position(0)` após marcar no índice 2), a marca é **perdida**.
    - Chamar `reset()` sem uma marca definida causa `InvalidMarkException`.

3. **Herança Uniforme**:
    - Os métodos `mark()` e `reset()` são padronizados na classe `Buffer`, evitando redundâncias nas subclasses.

##### Object Methods

As classes de buffer fornecem os métodos convencionais `equals()`, `hashCode()` e `toString()`. Elas também implementam `Comparable`, fornecendo assim métodos `compareTo()`. No entanto, buffers **não** são `Serializable` ou `Cloneable`.

**Critérios de igualdade (`equals()`):**
- Os buffers devem ser do mesmo tipo (ex.: um `ByteBuffer` nunca é igual a um `IntBuffer`, mas pode ser igual a outro `ByteBuffer`).
- Devem ter o mesmo número de elementos restantes no buffer.
- Os elementos restantes nas mesmas posições relativas devem ser iguais.

**Observação importante:** A igualdade **não** considera:
- Elementos anteriores à posição atual
- Capacidade (`capacity`)
- Limites (`limit`)
- Marcas (`mark`)

**Exemplo ilustrativo:**
```java
CharBuffer buffer1 = CharBuffer.wrap("12345678");
CharBuffer buffer2 = CharBuffer.wrap("5678");
buffer1.get(); buffer1.get(); buffer1.get(); buffer1.get(); // Avança 4 posições
System.out.println(buffer1.equals(buffer2)); // Imprime 'true'
```

**Sobre `hashCode()`:**
- Implementado conforme o contrato de igualdade (buffers iguais têm hash codes iguais)
- O hash code muda quando elementos são adicionados/removidos
- **Não são bons como chaves** em tabelas hash devido a essa mutabilidade

**Comparação (`compareTo()`):**
- Compara elementos restantes um a um
- Se todos forem iguais, os buffers são considerados iguais
- Se encontrar elementos diferentes, retorna o resultado da primeira comparação desigual
- Se um buffer acabar antes de encontrar desigualdade, o mais curto é considerado menor

**Método `toString()`:**
- Retorna strings no formato: `java.nio.HeapByteBuffer[pos=0 lim=62 cap=62]`
- Útil principalmente para depuração
- **Exceção notável:** `CharBuffer` retorna uma string com os caracteres restantes


##### Channels

Os canais (**channels**) transferem blocos de dados entre buffers e várias fontes de E/S, como arquivos, sockets, datagramas e outros. Embora a hierarquia de classes de canais seja complexa — com múltiplas interfaces e muitas operações opcionais —, para programação de rede, **apenas três classes são essenciais**:

1. **`SocketChannel`**
2. **`ServerSocketChannel`**
3. **`DatagramChannel`**

Para conexões TCP (o foco até agora), apenas as duas primeiras são necessárias.

**Explicação Complementar:**
- **Função dos Canais**:  
  Atuam como "túneis" para mover dados entre buffers (memória) e fontes externas (rede/arquivos).

- **Simplificação para TCP**:
    - `SocketChannel`: Gerencia conexões de cliente.
    - `ServerSocketChannel`: Escuta conexões de servidor.
    - *`DatagramChannel` (UDP) é usado para comunicação sem conexão*.

- **Complexidade Controlada**:  
  Apesar da hierarquia intrincada, na prática essas três classes resolvem a maioria dos casos de rede.

##### SocketChannel

A classe **SocketChannel** é responsável por **ler e escrever** em sockets TCP. Os dados precisam ser codificados em objetos **ByteBuffer** para essas operações de leitura e escrita.

**Principais características:**
1. **Associação com Socket**
    - Cada SocketChannel está vinculado a um objeto **Socket** (que permite configurações avançadas).
    - Para aplicações que usam configurações padrão, essa associação pode ser ignorada.

2. **Uso Prático**
    - Foca na transferência eficiente de dados via buffers.
    - Ideal para operações de rede não-bloqueantes (NIO).

**Exemplo de Fluxo:**
```java
SocketChannel canal = SocketChannel.open();  
canal.connect(new InetSocketAddress("exemplo.com", 80));  
ByteBuffer buffer = ByteBuffer.allocate(1024);  
canal.read(buffer);  // Lê dados do socket para o buffer  
```


Essa classe é fundamental para programação de rede performática em Java, especialmente com NIO.


##### Connecting

A classe **SocketChannel** não possui construtores públicos. Em vez disso, você cria um novo objeto SocketChannel usando um dos dois métodos estáticos `open()`:

**Método 1: Conexão Imediata (Bloqueante)**
```java
public static SocketChannel open(SocketAddress remote) throws IOException
```
- Estabelece a conexão imediatamente (opera de forma bloqueante)
- Exemplo:
```java
SocketAddress address = new InetSocketAddress("www.cafeaulait.org", 80);
SocketChannel channel = SocketChannel.open(address); // Bloqueia até conectar
```

**Método 2: Conexão Posterior (Não-bloqueante)**

```java
public static SocketChannel open() throws IOException
```
- Cria um socket não conectado inicialmente
- Permite configurar opções antes da conexão
- Exemplo básico:
```java
SocketChannel channel = SocketChannel.open();
SocketAddress address = new InetSocketAddress("www.cafeaulait.org", 80);
channel.connect(address); // Conecta posteriormente
```

**Uso Avançado (Modo Não-Bloqueante)**
```java
SocketChannel channel = SocketChannel.open();
channel.configureBlocking(false); // Configura como não-bloqueante
SocketAddress address = new InetSocketAddress("www.cafeaulait.org", 80);
channel.connect(address); // Retorna imediatamente
```

**Finalizando Conexão Não-Bloqueante**
```java
public abstract boolean finishConnect() throws IOException
```
- Retorna `true` se a conexão estiver pronta
- Retorna `false` se a conexão ainda estiver em progresso
- Lança exceção se a conexão falhar

**Métodos de Verificação**

```java
public abstract boolean isConnected() // Retorna true se conectado
public abstract boolean isConnectionPending() // Retorna true se conexão estiver em andamento
```

**Principais Diferenças:**

| Característica          | Modo Bloqueante          | Modo Não-Bloqueante       |
|-------------------------|--------------------------|---------------------------|
| Comportamento           | Espera conexão completar | Retorna imediatamente     |
| Uso típico              | Aplicações simples       | Aplicações de alta performance |
| Controle                | Menos flexível           | Permite multitarefa       |

Esta abordagem fornece maior controle sobre operações de rede, especialmente importante para aplicações que exigem alta escalabilidade.

##### Reading

Para ler de um `SocketChannel`, primeiro crie um `ByteBuffer` para armazenar os dados e passe-o para o método `read()`:

```java
public abstract int read(ByteBuffer dst) throws IOException
```

**Comportamento:**
- Preenche o buffer com dados disponíveis
- Retorna o número de bytes lidos
- Retorna `-1` no final do fluxo (EOF)
- Modo bloqueante: lê pelo menos 1 byte ou retorna `-1`
- Modo não-bloqueante: pode retornar `0` se não houver dados imediatos

**Exemplo de loop de leitura:**
```java
while (buffer.hasRemaining() && channel.read(buffer) != -1);
```

###### Leitura com Múltiplos Buffers (Scatter)
Para preencher vários buffers de uma fonte (scatter):

```java
public final long read(ByteBuffer[] dsts) throws IOException
public final long read(ByteBuffer[] dsts, int offset, int length) throws IOException
```

**Diferença entre métodos:**
1. Primeira versão preenche todos os buffers do array
2. Segunda versão preenche `length` buffers, começando no índice `offset`

**Exemplo de uso:**
```java
ByteBuffer[] buffers = new ByteBuffer[2];
buffers[0] = ByteBuffer.allocate(1000);
buffers[1] = ByteBuffer.allocate(1000);

while (buffers[1].hasRemaining() && channel.read(buffers) != -1);
```

###### Pontos-chave:
- A posição atual do buffer é atualizada automaticamente durante a leitura
- O loop continua até preencher o último buffer ou encontrar EOF
- Útil para protocolos com cabeçalhos e corpos separados
- Melhora eficiência ao reduzir chamadas de sistema para leitura


##### Writing

Canais de socket são **full duplex** (bidirecionais) e possuem métodos tanto para leitura quanto para escrita. O processo de escrita é essencialmente o inverso da leitura:

1. **Preparação do Buffer**:
    - Preencha um `ByteBuffer` com dados
    - Use `flip()` para prepará-lo para leitura

2. **Escrita**:
   ```java
   public abstract int write(ByteBuffer src) throws IOException
   ```
    - Em modo **bloqueante**: escreve todos os dados ou lança exceção
    - Em modo **não-bloqueante**: pode escrever parcialmente (retorna bytes escritos)

3. **Loop de Garantia**:
   ```java
   while (buffer.hasRemaining() && channel.write(buffer) != -1);
   ```

###### Escrita com Múltiplos Buffers (Gather)

Para escrever dados de vários buffers de uma vez (útil para protocolos como HTTP com cabeçalho e corpo separados):

```java
public final long write(ByteBuffer[] dsts) throws IOException
public final long write(ByteBuffer[] dsts, int offset, int length) throws IOException
```

**Diferenças**:
- Primeiro método: escreve todos os buffers do array
- Segundo método: escreve `length` buffers a partir do índice `offset`

**Cenários Típicos**:
- Threads diferentes preenchendo buffers simultaneamente
- Protocolos com estrutura de mensagem fixa
- Otimização de E/S sobreposta (overlapped I/O)

###### Vantagens:
- **Eficiência**: Reduz chamadas de sistema para escrita
- **Flexibilidade**: Permite composição de mensagens complexas
- **Performance**: Ideal para operações não-bloqueantes

##### Closing

Assim como ocorre com sockets convencionais, você deve **fechar um canal** quando terminar de usá-lo para liberar a porta e quaisquer outros recursos que ele esteja utilizando:

```java
public void close() throws IOException
```

**Comportamento do fechamento:**

- Fechar um canal já fechado não tem efeito
- Tentativas de leitura/escrita em um canal fechado lançam exceções

**Verificação de estado:**

```java
public boolean isOpen()
```
Retorna `false` se o canal estiver fechado e `true` se estiver aberto (estes são os únicos dois métodos da interface `Channel` compartilhados por todas as classes de canal).

**Melhoria no Java 7+:**

A classe `SocketChannel` implementa `AutoCloseable`, permitindo seu uso em blocos **try-with-resources** para fechamento automático:

```java
try (SocketChannel canal = SocketChannel.open()) {
    // Operações com o canal
} // O canal é fechado automaticamente aqui
```

**Boas práticas:**
1. Sempre feche canais explicitamente ou use try-with-resources
2. Verifique `isOpen()` se houver dúvidas sobre o estado
3. Em aplicações de longa duração, o não fechamento pode causar vazamento de recursos


##### ServerSocketChannel

A classe **ServerSocketChannel** tem um único propósito: aceitar conexões de entrada. Você não pode ler, escrever ou conectar um **ServerSocketChannel** - sua única operação é aceitar novas conexões recebidas.

###### Principais características:

1. **Métodos essenciais**:
    - `accept()`: o método mais importante, responsável por aceitar novas conexões
    - `close()`: encerra o socket do servidor (herdado de todas as classes Channel)

2. **Funcionalidades herdadas**:
    - Métodos relacionados ao registro em um **Selector** para notificação de conexões recebidas
    - Operações básicas de canal compartilhadas por todas as implementações

3. **Limitações intencionais**:
    - Não suporta operações de leitura/escrita
    - Não permite iniciar conexões (apenas aceitá-las)

###### Exemplo básico de uso:

```java
ServerSocketChannel serverChannel = ServerSocketChannel.open();
serverChannel.bind(new InetSocketAddress(8080));
SocketChannel clientChannel = serverChannel.accept(); // Aceita conexão
```

Esta classe é fundamental para implementação de servidores TCP não-bloqueantes em Java NIO, trabalhando em conjunto com **Selector** para gerenciamento eficiente de múltiplas conexões.

##### Creating server socket channels

O método fábrica estático `ServerSocketChannel.open()` cria um novo objeto `ServerSocketChannel`, mas o nome pode ser um pouco enganoso. Este método **não abre** imediatamente um socket de servidor - ele apenas cria o objeto. Para usá-lo corretamente:

1. **Obtenha o ServerSocket associado**:
   ```java
   ServerSocket socket = serverChannel.socket();
   ```

2. **Configure o socket**:
    - Defina opções como tamanho do buffer de recepção ou timeout
    - Use os métodos setters da classe `ServerSocket`

3. **Associe a uma porta**:
   ```java
   SocketAddress address = new InetSocketAddress(80);
   socket.bind(address);
   ```

**Exemplo completo**:
```java
try {
    ServerSocketChannel serverChannel = ServerSocketChannel.open();
    ServerSocket socket = serverChannel.socket();
    SocketAddress address = new InetSocketAddress(80);
    socket.bind(address);
} catch (IOException ex) {
    System.err.println("Falha ao vincular à porta 80: " + ex.getMessage());
}
```

###### Pontos importantes:
- A criação do canal (`open()`) e a vinculação à porta (`bind()`) são etapas separadas
- A configuração deve ser feita no objeto `ServerSocket` retornado pelo método `socket()`
- A porta 80 requer privilégios de administrador em muitos sistemas

No Java 7, esse processo se tornou mais simples, pois `ServerSocketChannel` agora possui seu próprio método `bind()`:

```java
try {
    ServerSocketChannel server = ServerSocketChannel.open();
    SocketAddress address = new InetSocketAddress(80);
    server.bind(address);
} catch (IOException ex) {
    System.err.println("Não foi possível vincular à porta 80 devido a: " + ex.getMessage());
}
```

###### Principais pontos:

1. **Simplificação no Java 7+**:
    - Elimina a necessidade de obter o `ServerSocket` separadamente
    - Permite vincular diretamente o canal ao endereço

2. **Uso de método fábrica** (`open()`):
    - Implementado como factory method (não como construtor) para:
        - Permitir diferentes implementações específicas por JVM
        - Otimizar para hardware/SO local
    - **Observação**: A fábrica não é configurável pelo usuário - sempre retorna a mesma implementação em uma JVM

3. **Vantagem**:
    - Redução de código boilerplate
    - Mais intuitivo para operações básicas de servidor


##### Accepting connections

Após abrir e vincular um objeto `ServerSocketChannel`, o método `accept()` pode escutar por conexões de entrada:

```java
public abstract SocketChannel accept() throws IOException
```

###### Modos de Operação:
1. **Modo Bloqueante (padrão)**:
    - Fica aguardando até que uma conexão seja estabelecida
    - Retorna um `SocketChannel` conectado ao cliente
    - Ideal para servidores simples com respostas imediatas

2. **Modo Não-Bloqueante**:
    - Configurado com `configureBlocking(false)`
    - Retorna `null` se não houver conexões pendentes
    - Usado normalmente com `Selector` para processamento paralelo
    - Ideal para servidores que exigem processamento complexo por conexão

###### Tratamento de Exceções:
O método `accept()` pode lançar diversas exceções:

| Exceção | Causa |
|---------|-------|
| `ClosedChannelException` | Tentativa de usar um canal já fechado |
| `AsynchronousCloseException` | Canal fechado por outra thread durante o `accept()` |
| `ClosedByInterruptException` | Thread interrompida durante espera bloqueante |
| `NotYetBoundException` (Runtime) | Chamada a `accept()` antes de vincular o socket |
| `SecurityException` | Restrição do gerenciador de segurança na porta |

###### Exemplo de Uso Não-Bloqueante:
```java
ServerSocketChannel server = ServerSocketChannel.open();
server.bind(new InetSocketAddress(8080));
server.configureBlocking(false); // Modo não-bloqueante

while (true) {
    SocketChannel client = server.accept();
    if (client != null) {
        // Processar conexão
    }
    // Fazer outras tarefas
}
```

###### Pontos Chave:
- O modo padrão é bloqueante (comportamento tradicional)
- O modo não-bloqueante exige verificação explícita por `null`
- A combinação com `Selector` oferece o melhor desempenho para múltiplas conexões
- O tratamento adequado de exceções é essencial para robustez


##### The Channels Class

A classe **Channels** é uma utilitária simples para conversão entre canais (NIO) e fluxos tradicionais de I/O, permitindo interoperabilidade entre os modelos. É especialmente útil quando você deseja:

1. **Conversão Bidirecional**:
    - Transformar canais em streams/readers/writers
    - Converter streams tradicionais em canais NIO

2. **Métodos Principais**:
   ```java
   // De canais para streams
   public static InputStream newInputStream(ReadableByteChannel ch)
   public static OutputStream newOutputStream(WritableByteChannel ch)
   
   // De streams para canais
   public static ReadableByteChannel newChannel(InputStream in)
   public static WritableByteChannel newChannel(OutputStream out)
   
   // Conversão com suporte a charset
   public static Reader newReader(ReadableByteChannel ch, String encoding)
   public static Writer newWriter(WritableByteChannel ch, String encoding)
   ```

3. **Casos de Uso Típicos**:
    - Integração com APIs legadas que esperam streams
    - Processamento eficiente com NIO + conversão pontual
    - Exemplo com XML (SAX Parser):
      ```java
      SocketChannel channel = server.accept();
      processHTTPHeader(channel);
      XMLReader parser = XMLReaderFactory.createXMLReader();
      parser.setContentHandler(handler);
      InputStream in = Channels.newInputStream(channel); // Conversão crucial
      parser.parse(in);
      ```

4. **Compatibilidade**:
    - `SocketChannel` funciona com ambos os tipos (leitura/escrita)
    - `ServerSocketChannel` não é conversível (apenas aceita conexões)

###### Benefícios:
- **Performance**: Use NIO onde importa
- **Compatibilidade**: Mantenha integração com APIs legadas
- **Flexibilidade**: Conversão sob demanda entre modelos


##### Asynchronous Channels (Java 7)

O Java 7 introduziu as classes `AsynchronousSocketChannel` e `AsynchronousServerSocketChannel`, que oferecem operações de E/S não-bloqueantes com um modelo diferente do NIO tradicional:

###### Funcionamento Básico:
- **Comportamento assíncrono**: Operações de leitura/escrita retornam imediatamente
- **Mecanismos de resposta**:
    - `Future<Void>` para operações de conexão/aceitação
    - `Future<Integer>` para operações de leitura/escrita
    - `CompletionHandler` para processamento orientado a eventos

###### Exemplo com Future:

```java
// Conexão assíncrona
AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
Future<Void> conexão = client.connect(endereco);

// Processamento paralelo durante a conexão
realizarInicializaçõesLocais();

// Espera conclusão da conexão
conexão.get();

// Leitura assíncrona
ByteBuffer buffer = ByteBuffer.allocate(74);
Future<Integer> leitura = client.read(buffer);

// Processamento adicional...
leitura.get(); // Espera conclusão
buffer.flip();
Channels.newChannel(System.out).write(buffer);
```

###### Exemplo com CompletionHandler:

```java
class ManipuladorLinha implements CompletionHandler<Integer, ByteBuffer> {
    @Override
    public void completed(Integer resultado, ByteBuffer buffer) {
        buffer.flip();
        try {
            Channels.newChannel(System.out).write(buffer);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    @Override
    public void failed(Throwable erro, ByteBuffer buffer) {
        System.err.println("Falha: " + erro.getMessage());
    }
}

// Uso:
ByteBuffer buffer = ByteBuffer.allocate(74);
channel.read(buffer, buffer, new ManipuladorLinha());
```

###### Restrições Importantes:
1. **Concorrência limitada**:
    - Apenas 1 thread pode ler por vez
    - Apenas 1 thread pode escrever por vez
    - Tentativas concorrentes lançam `ReadPendingException`/`WritePendingException`

2. **Padrões recomendados**:
    - Usar `Future` para operações sequenciais
    - Usar `CompletionHandler` para processamento independente
    - Buffer como anexo ou variável local final

###### Casos de Uso Típicos:
- **Web crawlers** (coleta paralela de páginas)
- **Clientes de API** com múltiplas requisições simultâneas
- **Servidores de alto desempenho** com processamento assíncrono


##### Socket Options (Java 7)

A partir do Java 7, várias classes de canais implementam a nova interface `NetworkChannel` para configuração de opções de rede:

###### Classes que implementam:
- `SocketChannel`
- `ServerSocketChannel`
- `AsynchronousServerSocketChannel`
- `AsynchronousSocketChannel`
- `DatagramChannel`

###### Métodos principais:
```java
<T> T getOption(SocketOption<T> name) throws IOException
<T> NetworkChannel setOption(SocketOption<T> name, T value) throws IOException
Set<SocketOption<?>> supportedOptions()
```

###### Opções padrão (via `StandardSocketOptions`):
| Constante | Tipo | Descrição |
|-----------|------|-----------|
| `IP_MULTICAST_IF` | `NetworkInterface` | Interface para multicast |
| `IP_MULTICAST_LOOP` | `Boolean` | Loopback de pacotes multicast |
| `IP_MULTICAST_TTL` | `Integer` | TTL para pacotes multicast |
| `IP_TOS` | `Integer` | Tipo de serviço (QoS) |
| `SO_BROADCAST` | `Boolean` | Permite transmissão broadcast |
| `SO_KEEPALIVE` | `Boolean` | Mantém conexão ativa |
| `SO_LINGER` | `Integer` | Tempo de espera ao fechar (segundos) |
| `SO_RCVBUF` | `Integer` | Tamanho do buffer de recepção |
| `SO_REUSEADDR` | `Boolean` | Reutilização de endereço |
| `SO_SNDBUF` | `Integer` | Tamanho do buffer de envio |
| `TCP_NODELAY` | `Boolean` | Desativa algoritmo de Nagle |

###### Exemplo de uso:
```java
NetworkChannel canal = SocketChannel.open();
canal.setOption(StandardSocketOptions.SO_LINGER, 240); // 240 segundos
```

###### Observações importantes:
1. **Suporte variado**: Cada tipo de canal suporta opções diferentes
    - Exemplo: `ServerSocketChannel` não suporta `SO_SNDBUF`
2. **Exceção**: Tentar definir opção não suportada lança `UnsupportedOperationException`
3. **Compatibilidade**: As opções têm o mesmo significado que nas classes `Socket` tradicionais

###### Verificação de suporte:
```java
if(canal.supportedOptions().contains(StandardSocketOptions.SO_KEEPALIVE)) {
    canal.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
}
```


###### Classe OptionSupport (Suporte a Opções)
```java
import java.io.*;
import java.net.*;
import java.nio.channels.*;

public class SuporteOpcoes {
    public static void main(String[] args) throws IOException {
        // Testa opções em diferentes tipos de canais
        imprimirOpcoes(SocketChannel.open());
        imprimirOpcoes(ServerSocketChannel.open());
        imprimirOpcoes(AsynchronousSocketChannel.open());
        imprimirOpcoes(AsynchronousServerSocketChannel.open());
        imprimirOpcoes(DatagramChannel.open());
    }

    private static void imprimirOpcoes(NetworkChannel canal) throws IOException {
        System.out.println(canal.getClass().getSimpleName() + " suporta:");
        
        // Lista todas as opções suportadas e seus valores padrão
        for (SocketOption<?> opcao : canal.supportedOptions()) {
            System.out.println(opcao.name() + ": " + canal.getOption(opcao));
        }
        
        System.out.println();
        canal.close();
    }
}
```

###### Saída do Programa (Exemplo)
```text
SocketChannelImpl suporta:
SO_OOBINLINE: false
SO_REUSEADDR: false
SO_LINGER: -1
SO_KEEPALIVE: false
IP_TOS: 0
SO_SNDBUF: 131072
SO_RCVBUF: 131072
TCP_NODELAY: false

ServerSocketChannelImpl suporta:
SO_REUSEADDR: true
SO_RCVBUF: 131072

UnixAsynchronousSocketChannelImpl suporta:
SO_KEEPALIVE: false
SO_REUSEADDR: false
SO_SNDBUF: 131072
TCP_NODELAY: false
SO_RCVBUF: 131072

UnixAsynchronousServerSocketChannelImpl suporta:
SO_REUSEADDR: true
SO_RCVBUF: 131072

DatagramChannelImpl suporta:
IP_MULTICAST_TTL: 1
SO_BROADCAST: false
SO_REUSEADDR: false
IP_MULTICAST_IF: null
IP_TOS: 0
IP_MULTICAST_LOOP: true
SO_SNDBUF: 9216
SO_RCVBUF: 196724
```

###### Pontos-chave:
1. **Objetivo**: Demonstra as opções de socket suportadas por cada tipo de canal NIO
2. **Métodos principais**:
    - `supportedOptions()`: Lista opções disponíveis
    - `getOption()`: Mostra valores padrão
3. **Diferenças entre canais**:
    - Canais TCP (`SocketChannel`) vs UDP (`DatagramChannel`)
    - Canais síncronos vs assíncronos
4. **Valores padrão**:
    - Buffers de 128KB para TCP
    - Multicast habilitado para UDP
    - Nagle desativado por padrão


##### Readiness Selection

Para programação de redes, a segunda parte essencial das APIs NIO é a **seleção de prontidão** - a capacidade de identificar quais sockets estão prontos para operações de leitura/escrita sem bloqueio. Esse recurso é especialmente útil para:

1. **Servidores**: Manipulação eficiente de múltiplas conexões simultâneas
2. **Clientes avançados**: Navegadores web ou sistemas com múltiplas conexões paralelas

###### Funcionamento Básico:
1. **Registro de Canais**:
    - Canais são registrados em um objeto `Selector`
    - Cada canal recebe uma `SelectionKey` (chave de seleção)

2. **Consulta de Prontidão**:
   ```java
   Set<SelectionKey> chavesProntas = selector.select();
   ```
   Retorna apenas as chaves de canais prontos para operações não-bloqueantes

3. **Operações Monitoradas**:
    - `SelectionKey.OP_READ` (leitura)
    - `SelectionKey.OP_WRITE` (escrita)
    - `SelectionKey.OP_CONNECT` (conexão)
    - `SelectionKey.OP_ACCEPT` (aceitação)

###### Fluxo Típico:
```java
Selector selector = Selector.open();
SocketChannel canal = SocketChannel.open();
canal.configureBlocking(false);
canal.register(selector, SelectionKey.OP_READ);

while (true) {
    selector.select(); // Bloqueia até que algum canal esteja pronto
    Set<SelectionKey> chavesProntas = selector.selectedKeys();
    // Processa canais prontos...
}
```

###### Vantagens:
- **Eficiência**: Substitui o modelo "um thread por conexão"
- **Escalabilidade**: Permite milhares de conexões com poucas threads
- **Controle**: Seleção precisa de operações não-bloqueantes


##### The Selector Class

###### Criação do Seletor

O único construtor em `Selector` é protegido. Normalmente, um novo seletor é criado usando o método fábrica estático:
```java
public static Selector open() throws IOException
```

###### Registro de Canais

Canais são adicionados ao seletor através do método `register()` da classe `SelectableChannel` (não diretamente pelo `Selector`). Apenas canais de rede são selecionáveis - `FileChannel`, por exemplo, não é.

Métodos de registro:
```java
public final SelectionKey register(Selector sel, int ops) throws ClosedChannelException
public final SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException
```

###### Operações Monitoradas

Constantes de operação (bit flags combináveis com `|`):
- `SelectionKey.OP_ACCEPT` (aceitar conexões)
- `SelectionKey.OP_CONNECT` (estabelecer conexões)
- `SelectionKey.OP_READ` (operações de leitura)
- `SelectionKey.OP_WRITE` (operações de escrita)

Exemplo de registro múltiplo:
```java
channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
```

###### Seleção de Canais Prontos

Métodos de seleção:
1. **Não-bloqueante**:
   ```java
   public abstract int selectNow() throws IOException
   ```
   Retorna imediatamente (0 se nenhum canal pronto)

2. **Bloqueantes**:
   ```java
   public abstract int select() throws IOException // Espera indefinidamente
   public abstract int select(long timeout) throws IOException // Espera com timeout
   ```

###### Processamento

Para obter canais prontos:
```java
public abstract Set<SelectionKey> selectedKeys()
```
Importante: remover as chaves processadas do conjunto retornado.

###### Encerramento

Para liberar recursos:
```java
public abstract void close() throws IOException
```
- Cancela todas as chaves registradas
- Interrompe threads bloqueadas em operações `select`

###### Fluxo Típico
1. Criar seletor
2. Registrar canais com operações desejadas
3. Em loop:
    - Chamar `select()` (bloqueante ou não)
    - Processar `selectedKeys()`
    - Remover chaves processadas
4. Fechar seletor quando não for mais necessário


##### The SelectionKey Class

Os objetos `SelectionKey` funcionam como ponteiros para canais e são essenciais para o gerenciamento eficiente de operações de E/S. Aqui está o fluxo detalhado:

###### 1. Registro e Atributos
- **Criação da chave**: Retornada pelo `register()` ao vincular um canal ao seletor
- **Anexo (attachment)**: Permite armazenar estado da conexão (ex.: buffer de leitura)
```java
ByteBuffer buffer = ByteBuffer.allocate(8192);
SelectionKey key = channel.register(selector, SelectionKey.OP_READ, buffer);
```

###### 2. Verificação de Prontidão
Quando um canal é selecionado (`selectedKeys()`), testamos suas capacidades:
```java
if (key.isReadable()) {
    // Processar leitura
} 
if (key.isWritable()) {
    // Processar escrita
}
```

###### 3. Recuperação de Componentes
- **Obter o canal**:
  ```java
  SelectableChannel channel = key.channel();
  ```
- **Recuperar anexo**:
  ```java
  Object attachment = key.attachment();
  ```

###### 4. Fluxo Completo Típico
```java
Selector selector = Selector.open();
SocketChannel channel = SocketChannel.open();
channel.configureBlocking(false);
channel.register(selector, SelectionKey.OP_READ);

while (true) {
    selector.select(); // Espera por eventos
    Set<SelectionKey> keys = selector.selectedKeys();
    
    for (Iterator<SelectionKey> it = keys.iterator(); it.hasNext();) {
        SelectionKey key = it.next();
        it.remove(); // Crucial: remove da lista de processados
        
        if (key.isReadable()) {
            SocketChannel ch = (SocketChannel) key.channel();
            ByteBuffer buf = (ByteBuffer) key.attachment();
            ch.read(buf);
            // Processar dados...
        }
    }
}
```

###### 5. Limpeza

- **Cancelamento manual**:
  ```java
  key.cancel(); // Remove registro do seletor
  ```
- **Fechamento automático**:
    - Fechar o canal → cancela todas suas keys
    - Fechar o seletor → invalida todas as keys registradas

###### Diagrama de Estados

```
[Registro] → [Seleção] → [Processamento]
    ↑               ↓
    └──[Cancelamento]←┘
```

###### Boas Práticas
1. Sempre remover keys processadas com `iterator.remove()`
2. Usar attachments para manter estado da conexão
3. Preferir fechamento explícito de canais
4. Testar múltiplas operações quando usar registros combinados (OP_READ | OP_WRITE)

###### Exemplo de intercalar FileChannel com SocketCHannel


Sim, é perfeitamente possível (e comum) intercalar um `FileChannel` com um `SocketChannel` para enviar arquivos a usuários. Esse é um padrão típico em servidores de arquivos ou aplicações de streaming. Veja como implementar:

###### Fluxo Básico de Operação

1. **Cliente solicita arquivo** (via socket)
2. **Servidor lê arquivo** (FileChannel)
3. **Servidor envia arquivo** (SocketChannel)

###### Implementação com NIO

```java
try (
    FileChannel fileChannel = FileChannel.open(Paths.get("arquivo.txt"), StandardOpenOption.READ);
    SocketChannel socketChannel = /* canal conectado ao cliente */;
) {
    // Transfere dados diretamente entre canais (zero-copy)
    fileChannel.transferTo(0, fileChannel.size(), socketChannel);
    
    // Alternativa com buffer para controle mais fino
    ByteBuffer buffer = ByteBuffer.allocateDirect(8192); // Buffer de 8KB
    while (fileChannel.read(buffer) != -1) {
        buffer.flip();
        socketChannel.write(buffer);
        buffer.compact();
    }
}
```

###### Versão com Selector (Não-Bloqueante)

```java
// Registra ambos os canais no selector
fileChannel.configureBlocking(false);
socketChannel.configureBlocking(false);

SelectionKey fileKey = fileChannel.register(selector, SelectionKey.OP_READ);
SelectionKey socketKey = socketChannel.register(selector, SelectionKey.OP_WRITE);

// Armazena estado no attachment
fileKey.attach(new FileState(buffer, fileChannel));

while (true) {
    selector.select();
    Set<SelectionKey> readyKeys = selector.selectedKeys();
    
    for (SelectionKey key : readyKeys) {
        if (key.isReadable() && key == fileKey) {
            // Lê do arquivo para o buffer
            FileState state = (FileState) key.attachment();
            state.buffer.clear();
            state.fileChannel.read(state.buffer);
        }
        else if (key.isWritable() && key == socketKey) {
            // Escreve do buffer para o socket
            FileState state = (FileState) fileKey.attachment();
            state.buffer.flip();
            socketChannel.write(state.buffer);
            state.buffer.compact();
        }
    }
    readyKeys.clear();
}

// Classe para manter o estado
class FileState {
    ByteBuffer buffer;
    FileChannel fileChannel;
    // [...] outros campos necessários
}
```

###### Considerações Importantes

1. **Eficiência**:
    - `transferTo()` usa zero-copy quando possível (diretamente do sistema de arquivos para a rede)
    - Buffers diretos (`allocateDirect`) melhoram performance

2. **Controle de Fluxo**:
    - Em modo não-bloqueante, verifique sempre o retorno das operações
    - Gerencie adequadamente o buffer entre leitura/escrita

3. **Gerenciamento de Recursos**:
    - Sempre feche ambos os canais após o uso
    - Trate corretamente exceções de E/S

4. **Caso de Uso Típico**:
   ```text
   [Cliente] → (Solicita "arquivo.txt") → [Servidor]
   [Servidor] → (Lê arquivo.txt) → [FileChannel]
   [Servidor] → (Transfere para SocketChannel) → [Cliente]
   ```

Esta abordagem é amplamente utilizada em servidores HTTP, FTP e sistemas de distribuição de conteúdo. A versão com selector é especialmente útil para servidores que precisam lidar com múltiplos clientes simultaneamente.
