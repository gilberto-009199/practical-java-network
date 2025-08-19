
## Streams


Observe que o autor usou um termo novo `Caracteres Multibytes`.
>Livro, pagina 49
>Usados corretamente, leitores e escritores podem lidar com uma ampla variedade de codificações de caracteres, incluindo conjuntos de caracteres multibyte, como SJIS e UTF-8.

O autor define fluxos como E/S, IO. Extindo então os inputStream e OutputStream.
Uma para entrada e outro para saiba.
Ele emenciona como as stream são sempre sincronas.
E que no capitulo 13 abordarao como fazer aplicações sem IO não bloqueante.

> Eu, Lembre:
> Streams são e sempre serão sincronas. 
> Quando vc solicitar gravar dados os dados serão gravados totalmente para depois vc conseguir continuar com sua execução.
> Se não terminar de enviar ou ler os dados pedidos vc continuará travado ate terminar. 

### OutputStream


Essa é a classe base das streams de saida. Os seus metodos são:

```java

public abstract void write(int b) throws IOException
public void write(byte[] data) throws IOException
public void write(byte[] data, int offset, int length) throws IOException
public void flush() throws IOException
public void close() throws IOException
```

O metodo mais basico é o `write(int b)`, nele podemos gravar um valor na stream.

No java as variaveis do tipo numerica, tem interfacos de positivo e negativo. Por exemplo, um numero int no java vai de :
-128  a +127, isso porque sacrificamos 1 bit para indicar se e positivo ou não.
Se não sacrificarmos esse 1 bit e usasemos ele como os demais o limite seria:
0 a 255, já que o 1 bit a mais dobraria a capacidade de numeros que poderiamos armazenar.

Para redes de computadores valores + ou - não fazem sentido isso só serviria apr ao programa que vc interpretar o pacote que enviamos. Para conseguirmos passar um inteiro. o java pega os 8 bits menos siginificativos(Os ultimos 8 bits da quele dado). exemplo:

```java

outputStream.write(1000); 
// 1000 = 
//		0b0011_1110_1000 
//			→ escreve só os últimos 8 bits 
//				→ 0b1110_1000 =
//								 232
````

Só podemos escrever valores de 0 a 255 em 1 byte.

O autor fala sobre estrategias de usar o `write()`. Podemos escrever um array de bytes de uma so vez ao inves de gravar 1 por 1. Segundo o autor isso e muito mais rapido. 

```java

write(byte[] data)
write(byte[] data, int offset, int length)

```


> Ponto de atenção.
> Algumas vezes as streams possuem buffers, o autor descreveu uma situação 
> na qual vc envia dados a uma stream , mas o host algo não recebe.
> As vezes esses butffers podem ser em software e as vezes em hardware de rede.
> Oque se pode fazer e chamar o `flush()`, essa função libera o output de saida.

Consequentemente, se vc terminar de gravar os dados, é importante liberar a saida.
Por exemplo, suponha que você tenha escrito uma solicitação de 300 bytes para um servidor HTTP 1.1 que usa HTTP Keep-Alive.
Geralmente, você deseja aguardar uma resposta antes de enviar mais dados. No entanto, se o fluxo de saída tiver um buffer de 1.024 bytes, o fluxo pode estar aguardando a chegada de mais dados antes de enviá-los para fora do buffer. 
Nenhum dado adicional será gravado no fluxo até que a resposta do servidor chegue, mas a resposta nunca chegará porque a solicitação ainda não foi enviada!

O método `flush()` quebra o deadlock forçando o fluxo armazenado em buffer a enviar seus dados mesmo que o buffer ainda não esteja cheio.

> O autor recomenda sempre liberar o fluxo depois de escrever nele, pois não temos como saber conserteza se existe ou não um buffer.

O autor destaca outro ponto de atenção:

> Deixar de efetuar a descarga quando necessário pode levar a travamentos de programa imprevisíveis e irrepetíveis, extremamente difíceis de diagnosticar se você não tiver uma boa ideia de qual é o problema em primeiro lugar.
> Como corolário de tudo isso, você deve descarregar todos os fluxos imediatamente antes de fechá-los. Caso contrário, os dados deixados no buffer quando o fluxo for fechado podem ser perdidos.

Ele fala do metodo `close()`, obvio que precisamos fechar sempre o fluxo.

>Eu,
>Sempre feche os fluxos

Se tentarmos escrever em um fluxo fechado receberemso obviament euma excessão de IO.
O autor nso fala de 2 Outpusts Stream na qual podemos obter infromações mesmo depois de fecharmos a coneção:

>Livro, pagina 54, 
>Por exemplo, um ByteArrayOutputStream fechado ainda pode ser convertido em uma matriz de bytes real e um DigestOutputStream fechado ainda pode retornar seu resumo.

Fechar certos fluxos podem emitir exceções então e aconselhavel usar um try. Por exemplo:

```java

OutputStream out = null;

try { 
	out = new FileOutputStream("/tmp/data.txt");
} catch (IOException ex) { 
	System.err.println(ex.getMessage()); 
} finally {
	if (out != null) {
		try { out.close(); }
		catch (IOException ex) { 
			// ignore 
		} 
	} 
}

```

E interresante que o autor citou o dispose, algo que eu não escuto a muito tempo.

>Eu, interresante , talves uma pesquisa futura:
>Essa técnica às vezes é chamada de padrão dispose; e é comum para qualquer objeto que precise ser limpo antes de ser coletado como lixo. Você a verá usada não apenas para fluxos, mas também para soquetes, canais, conexões e instruções JDBC e muito mais.

Parece ate um trilha interna. Ele fala como o java 7, criou o famoso `try(){}`, por exemplo:

```java

try (OutputStream out = new FileOutputStream("/tmp/data.txt")) { 
	// work with the output stream... 
} catch (IOException ex) {
	System.err.println(ex.getMessage()); 
}
```

Antes nós criavamos a variavel e depois atribuimos ela:

```java
OutputStream out = null;

try { 
	out = new FileOutputStream("/tmp/data.txt");
}
```

Agora podemos declarar e atribuir no mesmo statement.

Não precisaremos uar o `finally`, o java entende que o dispose. 

O Java invoca automaticamente close() em qualquer objeto AutoCloseable declarado dentro da lista de argumentos do bloco try .

### InputStream

A classe base e a `java.io.InputStream`.

Agora não estamos mais enviando dados e cuidando das tratativas, estamos agora a entrar nas questões de leitura e suas tratativas.

```java

public abstract int read() throws IOException
public int read(byte[] input) throws IOException
public int read(byte[] input, int offset, int length) throws IOException 
public long skip(long n) throws IOException 
public int available() throws IOException
public void close() throws IOException
```
E nosso output usa vamos o `write()`, agora que vamos ler devemos usar o `read()`.

Agora quando invocamos o `read()` obtemos um byte que e representado por 0 a 255;

Quando recebemos `-1` isso indica que ja lemos toas as informaçẽos disponiveis.

>Eu,
>Não se esqueça que inpustream podem vir da rede então esse -1 pode representar o estado atual , mas n~çao o estado futuro do conteudo da input stream.
>É improtante dizer que o read() trava nossa execução ate ele retornar o dado.

Um exemplo simples de leitura e tratativa é, por exemplo:

```java
byte[] input = new byte[10];

for (int i = 0; i < input.length; i++) {
	int b = in.read();
	if (b == -1) break;
	input[i] = (byte) b; 
}
```

Mais uma vez pensando em converter um valor inteiro em um byte em java.
O inteiro que recebemos estrá entre -128 e 128. Para converter usamos:

```java

int i = b >= 0 ? b : 256 + b;
```

Agora obtemos o valor entre 0 e 255 em si.

Como visto antes vale mais a pena preencher tudo de um vez doque preencher 1 byte por vez.

Podemos usar o metodo `public int read(byte[] input);` para preencher tudo de uma vez.

>Eu, 
>Atenção, so por que mandamos ele escrever em nosso vetor de bytes.
>os dados que estamos recebendo, não necessariamente vamso conseguir fazer isso.

O autor fala disso :

>Livro, pagina 56
>Observe que eu disse que esses métodos tentam preencher o array, não que eles necessariamente têm sucesso. Uma tentativa pode falhar de várias maneiras. Por exemplo, não é incomum que, enquanto seu programa está lendo dados de um servidor web remoto via DSL, um bug em um switch na central telefônica de uma companhia telefônica desconecte você e centenas de seus vizinhos do resto do mundo. Isso causaria uma IOException

O autor nos fala de um caso interresante, que faz sentido, ja que muitos protocolos de rede nos dizem logo no começo o tmanaho da menssagem logo no começo da transmissão.

>Livro, pagina 56
>Mais comumente, no entanto, uma tentativa de leitura não falhará completamente, mas também não será totalmente bem-sucedida. Alguns dos bytes solicitados podem ser lidos, mas não todos. Por exemplo, você pode tentar ler 1.024 bytes de uma conexão de rede, quando apenas 512 chegaram do servidor; o restante ainda está em trânsito. Eles chegarão eventualmente, mas não estão disponíveis neste momento.

Devemos aguardar ate que os dados em transito chegem.
Quando chamaos `read()` o mesmo nos retorna um int que representa a quantidade de bytes lidos.

```java

byte[] entrada = novo byte[1024];
int bytesRead = in.read(entrada);

```

>Livro, pagina 57
>Ele tenta ler 1.024 bytes do InputStream para a entrada do array. No entanto, se houver apenas 512 bytes disponíveis, isso será tudo o que será lido, e bytesRead será definido como 512. Para garantir que todos os bytes desejados sejam realmente lidos, coloque a leitura em um loop que lê repetidamente até que o array esteja preenchido.

O autor nos da um exemplo:

```java

int bytesRead = 0;
int bytesToRead = 1024;
byte[] input = new byte[bytesToRead];
while (bytesRead < bytesToRead) { 

	bytesRead += in.read(input, bytesRead, bytesToRead - bytesRead); 
	
}
```

Como pode ver usamos o metodo `read(byte[] input, int offset, int lentght)`.
>Eu,
>Forma essencial para redes. 
>Colocar em um loop ate que o pacote seja completamente lido.

O autor nos fala de um bug, no codigo acima. E se os dados restantes nunca chegarem? O autor nos fala de verificar se o valor retornado com read for -1, antes de debinir o bytesRead.


```java
int bytesRead = 0;
int bytesToRead = 1024;
byte[] input = new byte[bytesToRead];
while (bytesRead < bytesToRead) {
	int result = in.read(input, bytesRead, bytesToRead - bytesRead);
	
	if (result == -1) break; // end of stream

	bytesRead += result;
}
```

Ao inves de iterarmos ate a exaustão, podemos usar `avaliable()`, essa função nos retornará quantos bytes podemos ler no momento atual. Por exemplo:

```java

int bytesAvailable = in.available();
byte[] input = new byte[bytesAvailable];
int bytesRead = in.read(input, 0, bytesAvailable);
// continue with rest of program immediately...

```

Em bora interresante, creio que isso pode ser problematico ja que no final do fluxo retornara 0.
Ou mesmo se eu estiver lendo um pacote afim, mas o array de bytes contiver 2 pacotes, sneod eles o 1°(Meu atual lendo) e um 2° pacote que lerei em seguencia, isso pode se tornar problematico.

pegar os dados por avalibliate ao te mesmo se o pacote for amior que o array de bytes eu devo aguardar no for ate ele retornar os dados restantes que não entrarm no array de bytes.

>Eu, nota
>Pensar no futuro sobre esse caso de uso

E possivel pular determinada parte do conteudo usando a função `skip()`, aonde podemos pular partes da stream.

Para um arquivo isso poderia ser bem util. Se eu ja não usase o RamdomAcess, para escrever qualquer seguencia de bytes em qualquer parte do arquivo. Usaria `skip()` seria uma boa ideia.

Se necessario podemos reler dados. Usnado os metodos `mark()` e `reset()`.
Quando começarmos a ler usamos `mark()` para marcar a posição atual e então continuamos o fluxo, depois simplemente damos `reset()` e então voltaremos a posição de marcamos anteriormente.
Os metodos são:
```java

public void mark(int readAheadLimit)
public void reset() throws IOException
public boolean markSupported()
```

Nem todos os streams suportão mark logo e importante verificar com markSuported().
Ao tentarmos voltar atras usamos `reset()` , mas se o ponto de marcação estiver muito para tras pode ocorrer um erro.
Existe tambem a restrição, de somente uma marca. Se vc marcar pela 2° vez a 1° marca será apagada.

>Livro, pagina 59
>Na minha opinião, isso demonstra um design muito ruim. Na prática, mais fluxos não suportam marcação e redefinição do que suportam. Anexar funcionalidade a uma superclasse abstrata que não está disponível para muitas, provavelmente a maioria, das subclasses é uma péssima ideia. Seria melhor colocar esses três métodos em uma interface separada que pudesse ser implementada pelas classes que fornecem essa funcionalidade.
> A desvantagem dessa abordagem é que você não poderia invocar esses métodos em um fluxo de entrada arbitrário de tipo desconhecido; mas, na prática, você não pode fazer isso de qualquer maneira, porque nem todos os fluxos suportam marcação e redefinição. Fornecer um método como markSupported() para verificar a funcionalidade em tempo de execução é uma solução mais tradicional e não orientada a objetos para o problema. 
> Uma abordagem orientada a objetos incorporaria isso ao sistema de tipos por meio de interfaces e classes, para que tudo pudesse ser verificado em tempo de compilação.

>Eu,
>Muito interresante essa posição `Uma abordagem orientada a objetos incorporaria isso ao sistema de tipos por meio de interfaces e classes, para que tudo pudesse ser verificado em tempo de compilação.`, Usar a orientação a objetos apra conseguir verificar coisas em tempo de compilação e não em tempo de execução.


### Filter Streams

Agora que estamos familiarizados com a entrada e a saida, pdoemso adicioanr componetes que trabanhem com esses fluxos e nos retonem uma entrada pre processada.

No java temos varios filtros já prontos.
Podemos dividi-los em filtros de stream, `readers` e `writers`.
Eles podem atuar compactando os dados ou interpretando-os como numeros binarios(inteiro de 4 bytes).
Já os `readers` e `writers` lidam com textos em diferentes codificações como UTF-8 e ISO 8859-1.

Nos podemos encadear os filtros.

![[../../_External/Image/Pasted image 20250716044401.png]]
>Livro, pagina 60
>Neste exemplo, um arquivo de texto compactado e criptografado chega da interface de rede local, onde o código nativo o apresenta ao TelnetInputStream não documentado. Um BufferedInputStream armazena os dados em buffer para acelerar todo o processo. Um CipherInput Stream descriptografa os dados. Um GZIPInputStream descompacta os dados decifrados. Um InputStreamReader converte os dados descompactados em texto Unicode. Por fim, o texto é lido no aplicativo e processado.


O auto nos da um exemplo com um arquivo de texto.
Primeiro ele cria uma File InputStream para ler os dados do arquivo:

```java

FileInputStream fin = new FileInputStream("data.txt"); BufferedInputStream bin = new BufferedInputStream(fin);
```

Ele começa colcoando o FIleInputStream em um BufferedInputStream 
> Eu,
> O autor nos fala de um bug, nos qual usamos o read() de fin e alteramos o fluxo de bin. Uma vez que eles estão encadeados devemso sempre so usar o ultimo.
> Ele nos dá uma estrategia para isso:
```java

InputStream in = new FileInputStream("data.txt");
in = new BufferedInputStream(in);
 ```

Nos genralizamos o `in`, isso nos premite a acesso somente ao que a classe InputStream nós dá, mas não  nos da acesso ao metodos especificos de BufferedInputStream.
Para isso o autor nos dá a essa alternativa:

```java

DataOutputStream dout = new DataOutputStream(
							new BufferedOutputStream( 
									new FileOutputStream("data.txt")
							)
						);
```

Em nosos arquivo de texto podemos ter um arquivo codificado em UTF-8 ou outra encodificação, logo podemos ler os primeiros caracteres.

> Livro, pagina 62
> Por exemplo, se você estiver lendo um arquivo de texto Unicode, talvez queira ler a marca de ordem de bytes nos três primeiros bytes para determinar se o arquivo está codificado como big-endian UCS-2, little-endian UCS-2 ou UTF-8 e, em seguida, selecionar o filtro Reader correspondente para a codificação.

>Eu,
>Também pode ser interresante para servidores web aonde lemos o contenttype apra saber a codificação do payload. Inclusive o autor da exatamente esse exemplo.


#### Buffered Streams

O autor nos flaa da utilidade de usar buffers, principalemnte em rede:

>Livro, pagina 62
>Uma única gravação de muitos bytes é quase sempre muito mais rápida do que muitas gravações pequenas que, somadas, resultam na mesma coisa. 
>Isso é especialmente verdadeiro em conexões de rede, pois cada segmento TCP ou pacote UDP carrega uma quantidade finita de sobrecarga, geralmente cerca de 40 bytes. Isso significa que enviar 1 quilobyte de dados, 1 byte por vez, na verdade requer o envio de 40 quilobytes pela rede, enquanto enviar tudo de uma vez requer apenas o envio de pouco mais de 1 K de dados.
> A maioria das placas de rede e implementações TCP fornecem algum nível de buffer, portanto, os números reais não são tão drásticos. No entanto, armazenar em buffer a saída da rede geralmente representa uma grande vantagem em termos de desempenho

Entenda que vale a pena fazer isso na nossa implementação do bittorrent.
Essa classe tem um buff interno aonde ele armazena os dados, podemos definir o tamanho do buffer, abaixo seque os construtores:
```java

public BufferedInputStream(InputStream in)
public BufferedInputStream(InputStream in, int bufferSize)
public BufferedOutputStream(OutputStream out)
public BufferedOutputStream(OutputStream out, int bufferSize)
```

O autro nos avisa que caso não definirmos o tamanho do buffer, ele usará tamanho default:

>Livro, pagina 63
>Caso contrário, o tamanho do buffer é definido como 2.048 bytes para um fluxo de entrada e 512 bytes para um fluxo de saída. 
>O tamanho ideal para um buffer depende do tipo de fluxo que você está armazenando em buffer.
>Para conexões de rede, você quer algo um pouco maior do que o tamanho típico do pacote. No entanto, isso pode ser difícil de prever e varia dependendo das conexões e protocolos de rede locais.
> Redes mais rápidas e com maior largura de banda tendem a usar pacotes maiores, embora os segmentos TCP geralmente não sejam maiores do que um quilobyte.

O autor diferencia os BufferStream com frase:

>Livro, pagina 63
>A diferença é que cada gravação coloca os dados no buffer em vez de diretamente no fluxo de saída subjacente.
>Consequentemente, é essencial liberar o fluxo quando você atinge o ponto em que os dados precisam ser enviados.


#### PrintStream

Essa e famosa, eu sei que o `System.out` e uma classe `PrintStream`. 
O autor nos mostra os cotnrutores da classe:

```java

public PrintStream(OutputStream out)
public PrintStream(OutputStream out, boolean autoFlush)
```

Por padrão esse tipo de stream precisa de explicitamente liberado. Mas de autoflash for true, então á cada `println()` ou `\n` o buffer será liberado.

```java

public void print(boolean b) 
public void print(char c)
public void print(int i)
public void print(long l)
public void print(float f)
public void print(double d)
public void print(char[] text)
public void print(String s)
public void print(Object o)
public void println()
public void println(boolean b)
public void println(char c)
public void println(int i)
public void println(long l)
public void println(float f)
public void println(double d)
public void println(char[] text)
public void println(String s)
public void println(Object o)

```

O autor nos da um dica:

>Livro, pagina 64
>PrintStream é maligno e programadores de rede devem evitá-lo como uma praga!
>
>O primeiro problema é que a saída de println() depende da plataforma. 
>Dependendo do sistema que executa seu código, as linhas podem, às vezes, ser quebradas com um avanço de linha, um retorno de carro ou um par retorno de carro/avanço de linha. Isso não causa problemas ao escrever no console, mas é um desastre para escrever clientes e servidores de rede que precisam seguir um protocolo preciso.
>
>O segundo problema é que o PrintStream assume a codificação padrão da plataforma em que está sendo executado.
>
>O terceiro problema é que o PrintStream devora todas as exceções. Isso o torna adequado para programas didáticos como o HelloWorld, pois uma saída simples de console pode ser ensinada sem sobrecarregar os alunos com o aprendizado prévio sobre tratamento de exceções e tudo o que isso implica

Interresante saber que para detectar erros precisamos chamar `checkError()`. Ja que o PrintStream não solta excesões.

#### Data Streams

Esse nós permite escrever dados primitivos e Strings na stream. Usamos as classes `DataInputStream` e `DataOutputStream`.
Ele e projetado apra trocar dados com outros programas java. 

>Livro, pagina 66
>Os formatos binários utilizados destinam-se principalmente à troca de dados entre dois programas Java diferentes por meio de uma conexão de rede, um arquivo de dados, um pipe ou algum outro intermediário

O autor no da algusn exemplo de protocolos que trabalham com a mesma conversão que o Java. 

>Livro pagina 66
>Tanto o Java quanto a maioria dos protocolos de rede foram projetados por programadores Unix e, consequentemente, ambos tendem a usar os formatos comuns à maioria dos sistemas Unix. No entanto, isso não se aplica a todos os protocolos de rede; portanto, verifique os detalhes de qualquer protocolo que você use. 
>Por exemplo, o Network Time Protocol (NTP) representa os tempos como números de ponto fixo sem sinal de 64 bits, com a parte inteira nos primeiros 32 bits e a parte fracionária nos últimos 32 bits. Isso não corresponde a nenhum tipo de dado primitivo em nenhuma linguagem de programação comum, embora seja simples de usar — pelo menos na medida necessária para o NTP.

Para enviarmos dados usaremos os `write()` para cada tipo de dado:

```java

public final void writeBoolean(boolean b) throws IOException
public final void writeByte(int b) throws IOException
public final void writeShort(int s) throws IOException
public final void writeChar(int c) throws IOException
public final void writeInt(int i) throws IOException
public final void writeLong(long l) throws IOException
public final void writeFloat(float f) throws IOException
public final void writeDouble(double d) throws IOException
public final void writeChars(String s) throws IOException
public final void writeBytes(String s) throws IOException
public final void writeUTF(String s) throws IOException
```

Todos os dados serão gravos na ordem Big Endian. 

|Tipo|Formato|Tamanho|Observações|
|---|---|---|---|
|`byte`|inteiro|1 byte|Complemento de dois|
|`short`|inteiro|2 bytes|Idem|
|`int`|inteiro|4 bytes|Idem|
|`long`|inteiro|8 bytes|Idem|
|`float`|IEEE 754|4 bytes|Decimal|
|`double`|IEEE 754|8 bytes|Decimal de maior precisão|
|`boolean`|0 (false) ou 1 (true)|1 byte|Lógico|
|`char`|Unicode UTF-16|2 bytes|Sem sinal|

Strings e bem mais complicado. podemos usar os metodos:
+ `writeChars()`
+ `writeBytes()`
+ `writeUTF()`
 Nenhuma delas e uma solução ideal:
 + Usando `writeChars(String s)`
	 + Escreve cada caractere da string como **Unicode (UTF-16)**, 2 bytes por caractere, em **big-endian**.
	 + **Não inclui o tamanho** da string no fluxo.
 + Usando `writeBytes(String s)`
	 + Escreve **somente o byte menos significativo** de cada caractere.
	 + Perde informações de caracteres fora do padrão **Latin-1 (ASCII estendido)**.
	 + Pode ser útil em protocolos que exigem ASCII, mas **geralmente deve ser evitado**.
	 + Também **não inclui o tamanho** da string.
 + Usando `writeUTF(String s)`
	 + Escreve a string com **comprimento codificado** e usando uma **variação de UTF-8**.
	 + Funciona bem **somente entre programas Java** usando `DataInputStream`.
	 + **Não é compatível com UTF-8 padrão**, então evite para comunicação com programas não Java.
	

Agora para ler os dados vamos usar o `DataOutputStream`, os seus metodos são:

```java

public final boolean readBoolean() throws IOException
public final byte readByte() throws IOException
public final char readChar() throws IOException
public final short readShort() throws IOException
public final int readInt() throws IOException 
public final long readLong() throws IOException 
public final float readFloat() throws IOException 
public final double readDouble() throws IOException 
public final String readUTF() throws IOException
```

Podemos ler bytes sem sinal e shorts sem sinal. 

```java

public final int readUnsignedByte() throws IOException
public final int readUnsignedShort() throws IOException

```

Isso pode ser util para ler dados escritos em linguagens em C.
Outro metodo é o `readFully()`, esse nos permite ler e preencher todo o conteudo de um array de bytes, se não conseguir ler todos ele solta uma excessão.
O autor nos mostra as assinaturas:

```java

public final int read(byte[] input) throws IOException
public final int read(byte[] input, int offset, int length) throws IOException
public final void readFully(byte[] input) throws IOException
public final void readFully(byte[] input, int offset, int length) throws IOException
```

O DataInputStream fornece um metodo de ler linhas, usando o readLine().
>Eu,
>O autor fala que não devemos usar esse metodo pelo fato de apresentar bugs e porque não covnerte corretamente caracteres não ASCII na maioria das ciscunstancias. Ele recomenda o uso do `readLine()` do BufferedReader, mas ele adverte tbm que ambos apresentão bugs ao lidar com multiplos caracteres `\r`.
>O autor recoemnda isso somente apra arquivos, segundo ele em um cliente de rede ocorreria:
>Livro, pagina 68:
>No entanto, em conexões de rede persistentes, como as usadas para FTP e HTTP de modelo mais recente, um servidor ou cliente pode simplesmente parar de enviar dados após o último caractere e aguardar uma resposta sem realmente fechar a conexão.
> Se você tiver sorte, a conexão pode eventualmente expirar em uma ou outra extremidade e você receberá uma IOException, embora isso provavelmente leve pelo menos alguns minutos e faça com que você perca a última linha de dados do fluxo. Se você não tiver sorte, o programa travará indefinidamente.



#### Readers and Writers

Bem antigamente tudo custumava usar ASCII, naquele momento unico do tempo enque todos os protocolos trocavam ASCII entre si, 1 byte era igual a 1 caracter, ao seja, 1 byte == 1 caracter.
Nesse momento de 2025 isso raramente e verdade não é? Gil.
Verdade a javavm trabalha por padrão do UTF-16.
No protocolo http, o falor e defino em content encoded ou algo assim.

>Livro, pagina 68
>Embora alguns protocolos de rede mais antigos e simples, como daytime, quote of the day e chargen, especifiquem a codificação ASCII para texto, isso não se aplica ao HTTP e a muitos outros protocolos mais modernos, que permitem uma ampla variedade de codificações localizadas, como KOI8-R Cirílico, Big-5 Chinês e ISO 8859-9 para Turco

O autor nos diz que o java nos fornece uma copia das apis de stream, mostradas ate agora, mas para tratar caracter em vez de bytes.

Nossas novas super classes sao a `Reader` e a `Writer`, do pacote `java.io`, 

##### Reader

Nossa classe principal  a `java.io.InputStreamReader`, mas so podemos usar as suas filhas também temos:

+ FileReader 
+ FileWriter 
+ StringReader 
+ StringWriter 
+ CharArrayReader
+ CharArrayWriter

As duas primeiras classes desta lista trabalham com arquivos e as quatro últimas trabalham dentro de Java, portanto, não são muito úteis para programação em rede


##### Writers

Nossa classe principal  a `java.io.OutputStreamWriter`. 

```java

protected Writer()
protected Writer(Object lock)
public abstract void write(char[] text, int offset, int length) throws IOException 
public void write(int c) throws IOException
public void write(char[] text) throws IOException
public void write(String s) throws IOException
public void write(String s, int offset, int length) throws IOException
public abstract void flush() throws IOException
public abstract void close() throws IOException

```

O autor nos avisa que ao tentar implementar devemos reimplementar o metodo `write(char[] text, int offset, int length)`, pois os outros metodos chamam esse.

>Livro, pagina 70
>No entanto, quantos e quais bytes são gravados por essas linhas depende da codificação que o w usa. Se estiver usando UTF-16 big-endian, ele gravará esses 14 bytes (mostrados aqui em hexadecimal) nesta ordem:
>   00 4E 00 65 00 74 00 77 00 6F 00 72 00 6B
>
>Por outro lado, se w usa little-endian UTF-16, esta sequência de 14 bytes é escrita:
>
>   4E 00 65 00 74 00 77 00 6F 00 72 00 6B 00
> 
>Se w usar Latin-1, UTF-8 ou MacRoman, esta sequência de sete bytes será escrita:
>   4E 65 74 77 6F 72 6B
> 
> A saída exata depende da codificação.

Ao usar, devemos sempre usar o `.flash()`, pois e comum ter um buffer e só grantimos que enviamos os dados do buffer usando ele.

Agora que podemos ler vamos escrever tbm, com essa classe podemos determinar a codificação de saida.
Seu construtor especifica o fluxo de saída a ser gravado e a codificação a ser usada:

```java

public OutputStreamWriter(OutputStream out, String encoding) throws UnsupportedEncodingException

```

O autro nos diz apra sempre determianr qual o encoding, do contrario o java pegará o  padrão da plataforma(Do sistema operacional).

>Livro, pagina 71
> Conjuntos de caracteres padrão podem causar problemas inesperados em momentos inesperados.
> Geralmente, é quase sempre melhor especificar explicitamente o conjunto de caracteres do que deixar o Java escolher um para você.
> Por exemplo, este fragmento de código escreve as primeiras palavras da Odisseia de Homero na codificação grega CP1253 do Windows

```java

Writer w = new OutputStreamWriter( 
				new FileOutputStream("OdysseyB.txt"),
				"Cp1253"
			);

w.write("ἦμος δ΄ ἠριγένεια φάνη ῥοδοδάκτυλος Ἠώς");

```

Podemos pegar a codificação chamando o `getEncoding()`.

##### Readers

Assim como InputStream e Writer, a classe Reader nunca é usada diretamente, apenas por meio de uma de suas subclasses. Ela possui três métodos read() , além dos métodos skip(), close(), ready(), mark(), reset() e markSupported() :

```java

protected Reader()
protected Reader(Object lock)
public abstract int read(char[] text, int offset, int length) throws IOException
public int read() throws IOException
public int read(char[] text) throws IOException 
public long skip(long n) throws IOException
public boolean ready()
public boolean markSupported() 
public void mark(int readAheadLimit) throws IOException 
public void reset() throws IOException 
public abstract void close() throws IOException
```

>Livro, pagina 72
>A exceção à regra de similaridade é ready(), que tem o mesmo propósito geral que available() , mas não exatamente a mesma semântica, mesmo que module a conversão de bytes para caracteres.
> Enquanto available() retorna um int especificando um número mínimo de bytes que podem ser lidos sem bloqueio, ready() retorna apenas um booleano indicando se o leitor pode ser lido sem bloqueio.
> O problema é que algumas codificações de caracteres, como UTF-8, usam números diferentes de bytes para caracteres diferentes. Portanto, é difícil dizer quantos caracteres estão aguardando no buffer da rede ou do sistema de arquivos sem realmente lê-los do buffer.


para determinar a codificação, usamos o construtor:

```java

public InputStreamReader(InputStream in) 
public InputStreamReader(InputStream in, String encoding) throws UnsupportedEncodingException
```

O autor nos da um exemplo aonde ele lé uma stream e converte tudo em uma string Unicode usando a codificação MacCyrillic:

```java

public static String getMacCyrillicString(InputStream in) throws IOException { 
	InputStreamReader r = new InputStreamReader(in, "MacCyrillic");
	StringBuilder sb = new StringBuilder();
	int c; 
	while ((c = r.read()) != -1) sb.append((char) c);
	
	return sb.toString(); 
}
```


#### Filter Readers and Writers

Para converter streams de bytes para caracteres, usaremos as classes  `InputStreamReader` e `OutputStreamWriter`.

Depois disso, você pode adicionar filtros de leitura/escrita de texto usando:
- `BufferedReader`
- `BufferedWriter`
- `LineNumberReader`
- `PushbackReader`
- `PrintWriter`

As classes BufferedReader e BufferedWriter são os equivalentes baseados em caracteres das classes BufferedInputStream e BufferedOutputStream , orientadas a bytes.
Enquanto BufferedInputStream e BufferedOutputStream usam uma matriz interna de bytes como buffer, BufferedReader e BufferedWriter usam uma matriz interna de caracteres
Devmos definir o tamanho do buffer como vimso antes:

```java

public BufferedReader(Reader in, int bufferSize) 
public BufferedReader(Reader in) 
public BufferedWriter(Writer out) 
public BufferedWriter(Writer out, int bufferSize)

```

O autor resescreve o exmplo anterior usando um buffer:

>Livro, pagina 74
>Por exemplo, o exemplo anterior getMacCyrillicString() era pouco eficiente porque lia um caractere de cada vez. Como MacCyrillic é um conjunto de caracteres de 1 byte, ele também lia um byte de cada vez. No entanto, é fácil acelerá-lo encadeando um BufferedReader ao InputStreamReader, assim:
```java

public static String getMacCyrillicString(InputStream in) throws IOException { 
	Reader r = new InputStreamReader(in, "MacCyrillic");
	r = new BufferedReader(r, 1024);
	StringBuilder sb = new StringBuilder();
	int c;
	
	while ((c = r.read()) != -1) sb.append((char) c);
	
	return sb.toString(); 

}
```

A classe BufferedWriter() adiciona um novo método não incluído em sua superclasse, chamado newLine(), também voltado para a escrita de linhas:

```java

public void newLine() lança IOException

```


A classe PrintWriter substitui a classe PrintStream do Java 1.0 , que lida adequadamente com conjuntos de caracteres multibyte e texto internacional. A Sun planejou originalmente substituir PrintStream por PrintWriter , mas desistiu ao perceber que essa medida invalidaria muito código existente, especialmente o que dependia de System.out. No entanto, o novo código deve usar PrintWriter em vez de PrintStream.

Além dos construtores, a classe PrintWriter possui uma coleção de métodos quase idêntica à PrintStream. Entre eles estão:



```java 

public PrintWriter(Writer out)
public PrintWriter(Writer out, boolean autoFlush)
public PrintWriter(OutputStream out)
public PrintWriter(OutputStream out, boolean autoFlush)
public void flush()
public void close()
public boolean checkError()
public void write(int c)
public void write(char[] text, int offset, int length)
public void write(char[] text)
public void write(String s, int offset, int length)
public void write(String s)
public void print(boolean b)
public void print(char c)
public void print(int i)
public void print(long l)
public void print(float f)
public void print(double d)
public void print(char[] text)
public void print(String s)
public void print(Object o)
public void println()
public void println(boolean b)
public void println(char c)
public void println(int i)
public void println(long l)
public void println(float f)
public void println(double d)
public void println(char[] text)
public void println(String s)
public void println(Object o)

```
