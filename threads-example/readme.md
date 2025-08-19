
### Threads


O autor nos fala oomo antigamente se usava fork de processo para cada nova conexão.
E como isso gastava bastante recursos da maquina.

>Livro, pagina 77
>Por exemplo, quando baixávamos quilobytes de software gratuito de um site FTP popular pelos nossos modems de 2.400 bps usando o Kermit, frequentemente encontrávamos mensagens de erro como esta:
>```java 
>% ftp eunl.java.sun.com
> Connected to eunl.javasoft.com. 220 softwarenl FTP server (wu-2.4.2-academ[BETA- 16]+opie-2.32(1) 981105) ready. Name (eunl.java.sun.com:elharo): 
> 	anonymous 530- 530- Server is busy.
> 	 Please try again later or try one of our other 530- ftp servers at ftp.java.sun.com. Thank you. 530- 530 User anonymous access denied. Login failed.
>```
>O problema era que a maioria dos servidores FTP bifurcava um novo processo para cada conexão (ou seja, 100 usuários simultâneos significavam 100 processos adicionais para processar).
> Como os processos são itens bastante pesados, muitos deles poderiam rapidamente levar um servidor à falência.
> O problema não era que as máquinas não fossem potentes o suficiente ou que a rede não fosse rápida o suficiente; era que os servidores FTP eram mal implementados.
> Os primeiros servidores web também sofriam desse problema, embora o problema fosse um pouco mascarado pela natureza transitória das conexões HTTP.
> Quando um arquivo é recuperado em vez de permanecer conectado por minutos ou horas seguidas, os usuários da web não sobrecarregam o servidor tanto quanto os usuários de FTP. 
> No entanto, o desempenho do servidor web ainda se degrada com o aumento do uso. O problema fundamental é que, embora seja fácil escrever código que trate cada conexão de entrada e cada nova tarefa como um processo separado (pelo menos no Unix), essa solução não é escalável.
> Existem duas soluções para esse cenario:
> A primeira é reutilizar processos em vez de gerar novos. Quando o servidor é inicializado, um número fixo de processos (digamos, 300) é gerado para lidar com as solicitações. As solicitações recebidas são colocadas em uma fila. Cada processo remove uma solicitação da fila, atende a solicitação e retorna à fila para receber a próxima solicitação.
> A segunda solução para esse problema é usar threads leves em vez de processos pesados para lidar com conexões.
>  Enquanto cada processo separado tem seu próprio bloco de memória, as threads consomem menos recursos porque compartilham memória.
>  Usar threads em vez de processos pode aumentar em três vezes o desempenho do servidor.
>  Combinando isso com um conjunto de threads reutilizáveis (em oposição a um conjunto de processos reutilizáveis), seu servidor pode rodar nove vezes mais rápido, tudo no mesmo hardware e conexão de rede!

O autor nos da uma informação interesante:
> Livro, pagina 78
> A maioria das máquinas virtuais Java para devido ao esgotamento de memória em algum lugar entre 4.000 e 20.000 threads simultâneas. No entanto, ao usar um conjunto de threads em vez de gerar novas threads para cada conexão, menos de cem threads podem lidar com milhares de conexões curtas por minuto.

Interresante saber desses numeros.

O autor nos fala da necessidade de conhecer como trabalhar de forma segura para evitar.
As threads devem garantir que os recursos que utilizam atualmente e somente usada por ela, estrategia de lock. 
As threas devem ser cuidadosas mas devem tomar cuidado apra não entrarem em um dedadlock, aonde 1 thread tem o recurso A e precisa do B , enquanto tem 1 thread que tem o recusrso B , mas precisa do A, nesse caso ocorrerá um deadlock nem uma nem outra consegue proceseguir com sua tarefa.

O auto fala sobre como invocar therads e a diferença entre theads e runnables.
Oque para min faz sentido não tomarei noda por que que não existem nenhum dados ou informação alem doque um javeiro simples precisaria já saber.

>Livro, pagina 80
>Um programa com uma única thread termina quando o método main() retorna.
>Um programa com várias threads termina quando os métodos main() e run() de todas as threads não daemon retornam. (As threads daemon executam tarefas em segundo plano, como coleta de lixo, e não impedem a saída da máquina virtual.)

O autor nos da um exemplo de programa que sua multitread apra calcular o hash de um arquivo:
```java

import java.io.*;
import java.security.*;
import javax.xml.bind.*; // for DatatypeConverter; requires Java 6 or JAXB 1.0 
public class DigestThread extends Thread {
	private String filename;
	public DigestThread(String filename) {
		this.filename = filename;
	} 
	@Override 
	public void run() {
		try { 
			FileInputStream in = new FileInputStream(filename);
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			DigestInputStream din = new DigestInputStream(in, sha);
			
			while (din.read() != -1);
			
			din.close();
			byte[] digest = sha.digest();
			StringBuilder result = new StringBuilder(filename);
			result.append(": "); 
			result.append(DatatypeConverter.printHexBinary(digest));
			System.out.println(result);
		} catch (IOException ex) {
			System.err.println(ex); 
		} catch (NoSuchAlgorithmException ex) {
			System.err.println(ex);
		} 
	} 
	public static void main(String[] args) {
		for (String filename : args) {
			 Thread t = new DigestThread(filename);
			 t.start();
		} 
	}
}
```
> Livro, pagina 81
> Observe que toda a saída desta thread é primeiro construída em uma variável local StringBuilder, result. Isso é então impresso no console com uma invocação de método. O caminho mais óbvio de imprimir as partes uma de cada vez usando System.out.print() não é seguido. Há um motivo para isso, que discutiremos mais adiante


#### Returning Information from a Thread

As threads e seus metodos start e run não retonandm dados, Para retornar dados temos 2 estrategias:
+ Armazenar o processamento em um atributo na classe
+ Criar um metodos getter para acessar esse resultado depois que a therad termina.

Ele nos da 2 exemplo:

>Livro, pagina 84
```java

import java.io.*;
import java.security.*;
public class ReturnDigest extends Thread { 
	private String filename;
	private byte[] digest;
	public ReturnDigest(String filename) {
		this.filename = filename; 
	} 
	@Override
	public void run() { 
		try { 
			FileInputStream in = new FileInputStream(filename);
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			DigestInputStream din = new DigestInputStream(in, sha);
			
			while (din.read() != -1) ; // read entire file
			
			din.close();
			digest = sha.digest();
		} 
		catch (IOException ex) { System.err.println(ex); } 
		catch (NoSuchAlgorithmException ex) { System.err.println(ex); } 
	} 
	
	public byte[] getDigest() { return digest; } }

```

```java

import javax.xml.bind.*; // for DatatypeConverter 

public class ReturnDigestUserInterface { 
	public static void main(String[] args) {
		for (String filename : args) { 
			// Calculate the digest 
			ReturnDigest dr = new ReturnDigest(filename);
			dr.start(); // Now print the result 
			StringBuilder result = new StringBuilder(filename);
			result.append(": "); 
			byte[] digest = dr.getDigest();
			result.append(DatatypeConverter.printHexBinary(digest)); 
			System.out.println(result); 
		} 
	} 
}
```

É obvio que tal abordagem não nos permite fugir do null point execption, afinal não ha garantia deque a therad termine antes de invocarmos o getDigest().

Outra forma de obeter o retorno, seria deixar nossa thread de processamento, nós avisar quando temrinou. 

Criaremos uma metodo na nossa classe principal e deixariamos a thread principal continuar a partir dai.

O autor chamou essa abordagem de Callbacks. Oque faz sentido. Nesse cenario ele coloca uma static na classe que roda a thread.

```java

import java.io.*;
import java.security.*;
public class CallbackDigest implements Runnable {
	private String filename;
	public CallbackDigest(String filename) {
		this.filename = filename;
	} 
	@Override
	public void run() { 
		try {
			FileInputStream in = new FileInputStream(filename);
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			DigestInputStream din = new DigestInputStream(in, sha);
			
			while (din.read() != -1) ; // read entire file
			
			din.close();
			 
			byte[] digest = sha.digest(); 
			CallbackDigestUserInterface.receiveDigest(digest, filename); 
		} catch (IOException ex) { 
			System.err.println(ex);
		} catch (NoSuchAlgorithmException ex) {
			System.err.println(ex); 
		} 
	} 
}
```

```java
import javax.xml.bind.*; // for DatatypeConverter; requires Java 6 or JAXB 1.0 

public class CallbackDigestUserInterface { 
	
	public static void receiveDigest(byte[] digest, String name) { 
		StringBuilder result = new StringBuilder(name);
		result.append(": "); 
		result.append(DatatypeConverter.printHexBinary(digest)); 
		System.out.println(result);
	}
	
	public static void main(String[] args) {
		for (String filename : args) {
			// Calculate the digest
			CallbackDigest cb = new CallbackDigest(filename);
			Thread t = new Thread(cb);
			t.start(); 
		} 
	} 
}
```

Essa aboradem funcioan bem, mas o ideial segundo o autro reia n usar um static no metodo e passar a propria intancia, para que thread chame o metodo do objeto em si.

```java

import java.io.*;
import java.security.*;

public class InstanceCallbackDigest implements Runnable {

	private String filename;
	private InstanceCallbackDigestUserInterface callback;

	public InstanceCallbackDigest(String filename,
		InstanceCallbackDigestUserInterface callback) {
		
		this.filename = filename;
		this.callback = callback;
	} 
	@Override 
	public void run() {
		try {
			FileInputStream in = new FileInputStream(filename);
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			DigestInputStream din = new DigestInputStream(in, sha);
			
			while (din.read() != -1) ; // read entire file 
			
			din.close();
			byte[] digest = sha.digest();
			callback.receiveDigest(digest);
		} catch (IOException | NoSuchAlgorithmException ex) { 
			System.err.println(ex); 
		} 
	} 
}
```

```java

import javax.xml.bind.*; // for DatatypeConverter; requires Java 6 or JAXB 1.0 

public class InstanceCallbackDigestUserInterface {
	private String filename;
	private byte[] digest;
	public InstanceCallbackDigestUserInterface(String filename) { 
		this.filename = filename; 
	}
	
	public void calculateDigest() {
		InstanceCallbackDigest cb = new InstanceCallbackDigest(filename, this);
		Thread t = new Thread(cb);
		t.start(); 
	}
	
	void receiveDigest(byte[] digest) { 
		this.digest = digest;
		System.out.println(this); 
	}
	@Override 
	public String toString() { 
		String result = filename + ": ";
		if (digest != null) { 
			result += DatatypeConverter.printHexBinary(digest); 
		} else { 
			result += "digest not available";
		} 
		
		return result; 
	} 
	
	public static void main(String[] args) { 
		for (String filename : args) {
			// Calculate the digest
			InstanceCallbackDigestUserInterface d = new InstanceCallbackDigestUserInterface(filename);
			d.calculateDigest(); 
		} 
	} 
}
```


#### Futures, Callables, and Executors

O autor nos fala sobre o ExecutorService, como ele pdoe permitir executar operações com threads e etc, dando um exmplo de calculo matematico:

```java

import java.util.concurrent.Callable;

class FindMaxTask implements Callable<Integer> {

	private int[] data;
	private int start;
	private int end;
	
	FindMaxTask(int[] data, int start, int end) {
		this.data = data;
		this.start = start;
		this.end = end;
	}

	public Integer call() { 
		int max = Integer.MIN_VALUE;
		
		for (int i = start; i < end; i++) {
			if (data[i] > max) max = data[i];
		} 
		return max; 
	} 
}
```

```java

import java.util.concurrent.*;

public class MultithreadedMaxFinder {

	public static int max(int[] data)
	throws InterruptedException, ExecutionException {
	
		if (data.length == 1) { 
			return data[0];
		} else if (data.length == 0) { 
			throw new IllegalArgumentException(); 
		} 
		// split the job into 2 pieces 
		FindMaxTask task1 = new FindMaxTask(data, 0, data.length/2);
		FindMaxTask task2 = new FindMaxTask(data, data.length/2, data.length); 
		// spawn 2 threads 
		ExecutorService service = Executors.newFixedThreadPool(2);
		
		Future<Integer> future1 = service.submit(task1);
		Future<Integer> future2 = service.submit(task2);
		
		return Math.max(future1.get(), future2.get()); 
	} 
}
```

Usando a classe callable podemos submeter ao nossos executorService e receber uma future.

##### Synchronization

O autor nos da um exemplo de como threads escrevendo em `System.out`, acabam sobreescrevendo a saida uma das outras, quando usam caracter por caracter, e se elas escrever em bloco, a saida sai formatada, mas a ordem continua incerta.

Para conbtrollar esse caso ele nos da uma estrategia, sincronização de blocos.

##### Synchronized Blocks

Nesse cenario pode colocar um objeto como lock entre as threads, por exemplo:

```java

synchronized (System.out) { 
	System.out.print(input + ": "); 
	System.out.print(DatatypeConverter.printHexBinary(digest));
	System.out.println(); 
}

```

Assim cada thread so acessara essa excução quando o SYtem.out tiver sido liberado.

Ele nós da outro exemplo, usando uma classe de logs. 

```java

import java.io.*;
import java.util.*;
public class LogFile {
	private Writer out;
	public LogFile(File f) throws IOException {
		FileWriter fw = new FileWriter(f);
		this.out = new BufferedWriter(fw);
	}
	
	public void writeEntry(String message) throws IOException {
		Date d = new Date();
		out.write(d.toString());
		out.write('\t');
		out.write(message);
		out.write("\r\n");
	}
	
	public void close() throws IOException {
		out.flush();
		out.close(); 
	} 
}

```

E evidente que classes chamando os metodos de write passo a passo, iram conflitar.
Uma ira escrever /t para tabular a menssagem, enquanto outra escreve \r\n para mudar de linha.

Então ele nós apresenta uma novamente a solução:

```java

public void writeEntry(String message) throws IOException {
	synchronized (out) {
		Date d = new Date();
		out.write(d.toString());
		out.write('\t');
		out.write(message);
		out.write("\r\n"); 
	} 
}
```


nesse caos usamo o out put apra simcronizar, mas segundo o autro existe uma segunda possiblidade, sincronizar em nossa propria classe.

```java

public void writeEntry(String message) throws IOException {
	synchronized (this) {
		Date d = new Date();
		out.write(d.toString());
		out.write('\t');
		out.write(message);
		out.write("\r\n"); 
	} 
}

```

>Eu, me parece uma pessima ideia, exceto se nós formos uma singleton

###### Synchronized Methods

ao inves de selecioanrmos o objeto sobe o qual queremos sincronizar podemos só usar a palavra reservada na função:

```java

public synchronized void writeEntry(String message) throws IOException {
	Date d = new Date();
	out.write(d.toString());
	out.write('\t');
	out.write(message);
	out.write("\r\n"); 
}

```

> Eu, esse e a forma mais comun, embora ela caresa de lock em areas criticas, ela não esta vinculado a um recorso esclusivo , mas somente a entradada função em si.
> Logo, se eu quise-se proteger uma fila ou algo asism eu ainda precisaria usar sincronized no objeto fial em si, para que quaisquer metodos que chamem a fila tbm obedeção essa mesma regra.

>Eu, estou errado ao usar o sincronized vc sincroniza a si mesmo.
>Vc não fica so lockado na quela função, fica em todas as funções sincronizeds da quela instancia. Aou seja seria como um bloco `sincronized(this){ ... }` 

>Livro, pagina 99
>Simplesmente adicionar o modificador synchronized a todos os métodos não é uma solução abrangente para problemas de sincronização.
> Por um lado, isso causa uma grave perda de desempenho em muitas VMs (embora as VMs mais recentes tenham melhorado bastante nesse aspecto), potencialmente tornando seu código três vezes mais lento.
> Segundo, aumenta drasticamente as chances de deadlock.
> Terceiro, e mais importante, nem sempre é o objeto em si que você precisa proteger contra modificações ou acessos simultâneos, e sincronizar na instância da classe do método pode não proteger o objeto que você realmente precisa proteger.

###### Alternatives to Synchronization

Existem várias técnicas que evitam completamente a necessidade de sincronização.
A primeira é usar variáveis locais em vez de campos sempre que possível. Variáveis locais não apresentam problemas de sincronização. Toda vez que um método é inserido, a máquina virtual cria um conjunto completamente novo de variáveis locais para o método.
Essas variáveis são invisíveis de fora do método e são destruídas quando o método é encerrado. Como resultado, é impossível que uma variável local seja compartilhada por duas threads diferentes. 
Cada thread tem seu próprio conjunto separado de variáveis locais.

Argumentos de métodos de tipos primitivos também são protegidos contra modificações em threads separadas, pois Java passa argumentos por valor, e não por referência.

Argumentos de string são seguros porque são imutáveis (ou seja, uma vez criado um objeto String , ele não pode ser alterado por nenhuma thread). 
Um objeto imutável nunca muda de estado.

Os valores de seus campos são definidos uma vez quando o construtor é executado e nunca são alterados.

Os argumentos do StringBuilder não são seguros porque não são imutáveis; eles podem ser alterados após serem criados.

Em alguns casos, você pode usar uma classe thread-safe, mas mutável, do pacote java.util.concurrent.atomic.

Em particular, em vez de usar um int, você pode usar um AtomicInteger. Em vez de usar um long, você pode usar um AtomicLong. Em vez de usar um booleano, você pode usar um AtomicBoolean. 
Em vez de usar um int[], você pode usar um AtomicIntegerArray. Em vez de uma variável de referência, você pode armazenar um objeto dentro de uma AtomicReference, embora observe que isso não torna o objeto em si thread-safe, apenas a obtenção e a configuração da variável de referência.
Essas classes podem ser mais rápidas do que o acesso sincronizado aos seus respectivos tipos primitivos se puderem aproveitar as instruções thread-safe rápidas em nível de máquina em CPUs modernas

Para coleções como mapas e listas, você pode envolvê-las em uma versão thread-safe usando os métodos de java.util.Collections.
Por exemplo, se você tiver um conjunto foo, poderá obter uma visualização thread-safe desse conjunto com Collections.synchronizedSet(foo). Se você tiver uma lista foo, usaria Collections.synchronizedList(foo).
Para um mapa, chame Collections.synchronizedMap(foo) e assim por diante.
Para que isso funcione, você deve, doravante, usar apenas a visualização retornada por Collections.synchronizedSet/List/Map.

Em todos os casos, perceba que é apenas uma única invocação de método que é atômica. Se você precisar executar duas operações no valor atômico em sucessão sem possível interrupção, ainda precisará sincronizar. Assim, por exemplo, mesmo que uma lista seja sincronizada via Collections.synchronizedList(), você ainda precisará sincronizar nele se quiser iterar pela lista, pois isso envolve muitas operações atômicas consecutivas.
Embora cada chamada de método seja seguramente atômica, a sequência de operações não está isenta de sincronização explícita


##### Deadlock

O deadlock ocorre quando duas threads precisam de acesso exclusivo ao mesmo conjunto de recursos e cada thread mantém o bloqueio em um subconjunto diferente desses recursos.

> Eu, normalmente, crio um terceiro elemento que contem o conjunto inteiro e logo esse terceiro elemento so pode estar com o 1°  ou o 2° elemento, e não disperso entre ambos. Iqual como faço como uma dependencia ciclica.

Se nenhuma das threads estiver disposta a abrir mão dos recursos que possui, ambas param por tempo indeterminado. Isso não é exatamente um travamento no sentido clássico, pois o programa ainda está ativo e se comportando normalmente da perspectiva do sistema operacional, mas para o usuário a diferença é insignificante.



#### Thread Scheduling

Quando várias threads estão em execução ao mesmo tempo (mais propriamente, quando várias threads estão disponíveis para serem executadas ao mesmo tempo), você precisa considerar questões de agendamento de threads. 

Você precisa garantir que todas as threads importantes tenham pelo menos algum tempo para serem executadas e que as threads mais importantes tenham mais tempo. 

Além disso, você quer garantir que as threads sejam executadas em uma ordem razoável.

Se o seu servidor web tiver 10 requisições enfileiradas, cada uma das quais requer 5 segundos para ser processada, você não quer processá-las em série.

Se fizer isso, a primeira requisição terminará em 5 segundos, mas a segunda levará 10, a terceira 15 e assim por diante até a última requisição, que terá que esperar quase um minuto para ser atendida.

Nesse ponto, o usuário provavelmente já foi para outra página. Ao executar threads em paralelo, você pode ser capaz de processar todas as 10 requisições em apenas 10 segundos no total. 

O motivo pelo qual essa estratégia funciona é que há muito tempo morto no atendimento de uma solicitação web típica, tempo em que a thread está simplesmente esperando que a rede alcance a CPU — tempo que o escalonador de threads da VM pode utilizar para outras threads.

No entanto, threads dependentes da CPU (ao contrário das threads dependentes de E/S, mais comuns em programas de rede) podem nunca chegar a um ponto em que precisem esperar por mais entradas.

É possível que uma thread desse tipo deixe todas as outras threads sem energia, consumindo todos os recursos disponíveis da CPU. 
Com um pouco de reflexão, você pode evitar esse problema. Aliás, a inanição é um problema consideravelmente mais fácil de evitar do que a sincronização incorreta ou o deadlock.

###### Priorities

Cada thread tem uma prioridade, especificada como um número inteiro de 0 a 10. Quando várias threads estão prontas para execução, a VM geralmente executa apenas a thread com a maior prioridade, embora isso não seja uma regra rígida. Em Java, 10 é a prioridade mais alta e 0, a mais baixa.

A prioridade padrão é 5, e essa é a prioridade que suas threads terão, a menos que você as defina deliberadamente de outra forma.

Podemos criar 3 constantes para isso:
```java

public static final int MIN_PRIORITY = 1;
public static final int NORM_PRIORITY = 5;
public static final int MAX_PRIORITY = 10;
```

>Livro, pagina 103
>Nem todos os sistemas operacionais suportam 11 prioridades diferentes. Por exemplo, o Windows tem apenas 7. No Windows, as prioridades 1 e 2, 3 e 4, 6 e 7, e 8 e 9 são tratadas igualmente (por exemplo, uma thread com prioridade 9 não necessariamente substituirá uma thread com prioridade 8).

É possivel definir a prioridade usando `setPriority(int priority)`.
Tentar exceder a prioridade máxima ou definir uma prioridade não positiva gera uma `IllegalArgumentException`.

O livro nos da um exemplo, usando a ideia anterior de callbacks:

```java

public void calculateDigest() {
	ListCallbackDigest cb = new ListCallbackDigest(filename);
	cb.addDigestListener(this);
	Thread t = new Thread(cb);
	t.setPriority(8);
	t.start();
}
```

Em geral, porém, tente evitar usar uma prioridade muito alta para threads, porque você corre o risco de deixar outras threads de prioridade mais baixa sem uso.

> Eu interresante esse caso. Se eu definir pioridades muitos baixas ou muitos altas a java vm pode deixar threads desamparadas.

##### Preemption

A java vm tem uma agendador de threads que determina qual thread executa em um dado momento. Existem dois tipos principais de agendamento de threads: preemptivo e cooperativo.

Um agendador de threads preemptivo determina quando uma thread teve sua cota justa de tempo de CPU, pausa essa thread e então passa o controle da CPU para uma thread diferente.

Um agendador de threads cooperativo espera que a thread em execução pause a si mesma antes de passar o controle da CPU para uma thread diferente.

Uma máquina virtual que usa agendamento de threads cooperativo é muito mais suscetível à inanição de threads do que uma máquina virtual que usa agendamento de threads preemptivo, porque uma thread de alta prioridade e não cooperativa pode monopolizar uma CPU inteira.


Todas as máquinas virtuais Java têm a garantia de usar o agendamento preemptivo de threads entre prioridades. 
Ou seja, se uma thread de prioridade mais baixa estiver em execução quando uma thread de prioridade mais alta estiver pronta para ser executada, a máquina virtual, mais cedo ou mais tarde (e provavelmente mais cedo), pausará a thread de menor prioridade para permitir a execução da thread de maior prioridade.

A thread de maior prioridade interrompe a thread de menor prioridade.

A situação em que várias threads com a mesma prioridade estão prontas para execução é mais complicada. Um escalonador de threads preemptivo ocasionalmente pausa uma das threads para permitir que a próxima na fila tenha algum tempo de CPU.

No entanto, um escalonador de threads cooperativo não o fará. Ele aguardará que a thread em execução ceda explicitamente o controle ou chegue a um ponto de parada.

Se o thread em execução nunca abrir mão do controle e nunca chegar a um ponto de parada, e se nenhum thread de prioridade mais alta tomar o lugar do thread em execução, todos os outros threads morrerão de fome. Isso é ruim.

É importante garantir que todos os seus tópicos sejam pausados periodicamente para que outros tenham a oportunidade de serem executados.

>Livro, pagina 104
>Um problema de inanição pode ser difícil de detectar se você estiver desenvolvendo em uma VM que usa agendamento preemptivo de threads. Só porque o problema não ocorre na sua máquina não significa que não ocorrerá nas máquinas dos seus clientes se as VMs deles usarem agendamento cooperativo de threads.
>A maioria das máquinas virtuais atuais usa agendamento de threads preemptivo, mas algumas máquinas virtuais mais antigas são agendadas cooperativamente, e você também pode encontrar agendamento cooperativo em máquinas virtuais Java de propósito especial, como em ambientes incorporados.

Há 10 maneiras de uma thread pausar em favor de outras threads ou indicar que está pronta para pausar. São elas:

- Pode ser bloqueado por transações de E/S (entrada/saída). / Blocking
- Pode ser bloqueado ao acessar um objeto / sincronized(objeto)
- Pode ceder (yield) uma vez para outra / Yielding
- Pode dormir (sleep), ou seja, pausar sua execução /  Sleeping
- Pode aguardar (join) outra thread / Joining threads
- Pode esperar por um objeto  / Waiting on an object
- Pode terminar sua execução.
- Pode ser preempicionado por uam thread de prioridade mais alta
- Pode ser suspenso.
- Pode parar.

As duas últimas possibilidades estão obsoletas porque podem deixar objetos em estados inconsistentes. Portanto, vamos analisar as outras oito maneiras pelas quais uma thread pode ser uma cidadã cooperativa da máquina virtual.

###### Blocking 


**Blocking** ocorre quando uma thread precisa parar e esperar por um recurso que não está disponível.  

Causas comuns de blocking:  

1. **I/O (Entrada/Saída):**  
   - Threads frequentemente bloqueiam ao realizar operações de I/O (rede, disco), pois a CPU é muito mais rápida que esses dispositivos.  
   - Exemplo: Uma thread esperando dados da rede pode bloquear por alguns milissegundos, permitindo que outras threads executem tarefas durante esse tempo.  

2. **Sincronização (Locks):**  
   - Se uma thread tenta acessar um método/bloco **synchronized** e o lock já está com outra thread, ela bloqueia até que o lock seja liberado.  
   - Se o lock nunca for liberado, a thread fica permanentemente parada.  

Observações importantes:  

- **Locks mantidos durante blocking:**  
  - Se uma thread bloqueia (por I/O ou espera por um lock), **ela não libera os locks que já possui**.  
  - No caso de I/O, isso geralmente não é crítico, pois a thread eventualmente continua ou lança uma exceção (`IOException`), liberando os locks ao sair do bloco sincronizado.  
- **Deadlock:**  
  - Ocorre quando duas (ou mais) threads bloqueiam-se mutuamente, cada uma esperando por um lock que a outra possui.  
  - Exemplo: Thread A tem Lock X e espera por Lock Y, enquanto Thread B tem Lock Y e espera por Lock X → ambas ficam paradas indefinidamente.  

Blocking é essencial para gerenciar recursos, mas requer cuidado para evitar deadlocks e garantir que locks sejam liberados adequadamente.

###### Yielding

**Yielding** é quando uma thread **voluntariamente** cede seu tempo de CPU para outras threads, usando o método estático `Thread.yield()`.  
- É uma **sugestão** para a JVM, que pode ser ignorada (especialmente em sistemas de tempo real).  
- Não libera **locks** que a thread já possui.  

Quando usar?
- Útil em loops infinitos para evitar monopolizar a CPU.  
- Exemplo básico:  
  ```java
  public void run() {
      while (true) {
          // Trabalho da thread...
          Thread.yield(); // Permite que outras threads executem
      }
  }
  ```
- Se o loop for demorado, pode-se adicionar mais chamadas a `yield()` para melhorar a responsividade.  

Cuidados importantes:
1. **Estado consistente:**  
   - Antes de chamar `yield()`, a thread deve garantir que seus dados estejam em um estado seguro para uso por outras threads.  

2. **Problemas com locks:**  
   - Se a thread que faz `yield()` possui **locks sincronizados**, outras threads que dependam desses recursos **não poderão executar**, anulando o propósito do `yield()`.  
   - **Ideal:** Evite chamar `yield()` dentro de blocos `synchronized`.  

 Eficácia:
- Só beneficia threads de **mesma prioridade**.  
- Se não houver threads prontas para executar, a JVM pode retornar o controle à própria thread que fez o `yield()`.  


`Thread.yield()` é uma ferramenta simples para **melhorar a cooperação entre threads**, mas deve ser usada com cautela para evitar problemas de concorrência. Em muitos casos, técnicas como `wait()`/`notify()` ou estruturas de alto nível (ex: `ExecutorService`) são mais eficientes.


###### Sleeping

**Sleeping** é uma forma mais forte de pausar uma thread do que `yield()`.  

- Enquanto `yield()` sugere que a thread **cede a CPU para threads de mesma prioridade**, `sleep()` **pausa a thread por um tempo definido**, permitindo que **qualquer outra thread** (mesmo de prioridade menor) execute.  

- **A thread em sleep mantém todos os locks** que possui, o que pode bloquear outras threads que precisem deles.  



**Como usar Thread.sleep()**:

Dois métodos estáticos:  

```java
public static void sleep(long milliseconds) throws InterruptedException  
public static void sleep(long milliseconds, int nanoseconds) throws InterruptedException  
```  
- **Precisão limitada:** A maioria das JVMs não garante precisão em nanossegundos (ou mesmo milissegundos).  
- Se o hardware não suportar, o tempo é **arredondado** para o valor mais próximo possível.  

**Exemplo:** Uma thread que verifica uma página a cada 5 minutos:  
```java
public void run() {
    while (true) {
        if (!getPage("http://www.example.com/")) {
            notifyAdmin("admin@example.com");
        }
        try {
            Thread.sleep(300_000); // 5 minutos (300.000 ms)
        } catch (InterruptedException ex) {
            break; // Se interrompida, sai do loop
        }
    }
}
```  


**Problemas e Cuidados**: 

1. Sleep não garante tempo exato
   - A thread pode **dormir mais** do que o solicitado devido a atrasos no agendamento.  
1. Interrupção (`InterruptedException`) 
   - Outras threads podem **acordar** uma thread dormindo chamando `thread.interrupt()`.  
   - Isso lança `InterruptedException`, permitindo que a thread reaja (ex.: terminar limparmente).  
1. Evite sleep em blocos `synchronized`  
   - Como locks **não são liberados**, pode causar **deadlocks** se outras threads precisarem deles.  

**Sleep vs. I/O Blocking**:

- Se uma thread está bloqueada em **I/O** (ex.: `read()`, `write()`), chamar `interrupt()` **pode não funcionar** (depende do SO).  
  - Em **Solaris**, pode lançar `InterruptedIOException`, mas em outros sistemas, muitas vezes **não faz nada**.  
- **Alternativa melhor:** Usar **NIO (Non-Blocking I/O)** (Canais e Buffers), que suportam interrupção corretamente.  

No caso:
- `Thread.sleep()` é útil para **pausas programadas**, mas deve ser usado com cuidado:  
  - **Não use em blocos sincronizados** (risco de deadlock).  
  - **Trate `InterruptedException`** para permitir terminação graciosa.  
  - **Para I/O bloqueante, prefira NIO** se a interrupção for necessária.  
- Se o objetivo é **esperar por eventos**, mecanismos como `wait()`/`notify()` ou `Lock/Condition` são geralmente melhores.


###### Joining threads


**`join()`** permite que uma thread espere até que outra thread termine sua execução.  

- Útil quando uma thread depende do resultado de outra (ex.: processamento paralelo com consolidação final).  
- Existem três variações:  
  ```java
  void join()                          // Espera indefinidamente  
  void join(long milliseconds)         // Espera por um tempo máximo  
  void join(long ms, int nanoseconds)  // Espera com precisão de ns (não garantida)  
  ```  

**Como Funciona?**  
1. **Thread A** chama **`threadB.join()`** → **Thread A pausa** até que **Thread B** termine.  
2. Se **Thread B já terminou**, `join()` retorna imediatamente.  
3. Se **Thread A for interrompida** durante a espera, lança `InterruptedException`.  

**Exemplo:** Ordenação paralela com espera explícita  
```java
double[] array = new double[10000];  
for (int i = 0; i < array.length; i++) {  
    array[i] = Math.random();  
}  

SortThread t = new SortThread(array);  
t.start();  

try {  
    t.join(); // Espera a thread de ordenação terminar  
    System.out.println("Mínimo: " + array[0]);  
    System.out.println("Mediana: " + array[array.length/2]);  
    System.out.println("Máximo: " + array[array.length-1]);  
} catch (InterruptedException ex) {  
    System.err.println("Ordenação interrompida!");  
}  
```  


**Cuidados Importantes**  
1. **Ordem de Join**  
   - Se múltiplas threads são "joined" em sequência, a thread principal **espera na ordem chamada**.  
   - Isso pode atrasar o processamento se uma thread lenta bloquear as demais.  

2. **Interrupção**  
   - Se a thread que espera (`join()`) for interrompida (`interrupt()`), ela para de esperar e trata a exceção.  

3. **Alternativas Modernas (Java 5+)**  
   - **`ExecutorService` + `Future`** são preferíveis para tarefas assíncronas:  
     ```java
     ExecutorService executor = Executors.newFixedThreadPool(2);  
     Future<Resultado> futuro = executor.submit(() -> tarefaDemorada());  
     Resultado r = futuro.get(); // Equivalente a join(), mas com mais controle  
     ```  
   - **Vantagens:**  
     - Permite timeouts (`get(long timeout, TimeUnit unit)`).  
     - Suporte a cancelamento (`future.cancel()`).  

**Exemplo Prático (Consertando Race Conditions)**  
 
**Problema:** Uma thread principal tenta acessar resultados antes das threads filhas terminarem.  

**Solução:** Usar `join()` para sincronizar:  
```java
public class JoinDigestUserInterface {  
    public static void main(String[] args) {  
        ReturnDigest[] threads = new ReturnDigest[args.length];  

        for (int i = 0; i < args.length; i++) {  
            threads[i] = new ReturnDigest(args[i]);  
            threads[i].start();  
        }  

        for (int i = 0; i < args.length; i++) {  
            try {  
                threads[i].join(); // Espera cada thread terminar  
                String hash = DatatypeConverter.printHexBinary(threads[i].getDigest());  
                System.out.println(args[i] + ": " + hash);  
            } catch (InterruptedException ex) {  
                System.err.println("Thread interrompida!");  
            }  
        }  
    }  
}  
```  


**`join()`** é essencial para sincronização simples entre threads, mas tem limitações:  
  - **Espera bloqueante** (pode ser ineficiente).  
  - **Ordem rígida** (pode não ser ideal para tarefas heterogêneas).  

**Para sistemas complexos**, prefira:  
  - **`Future` + `ExecutorService`** (Java 5+).  
  - **Frameworks como CompletableFuture** (Java 8+).  

**Regra geral:** Use `join()` para sincronização básica; para cenários avançados, opte por APIs modernas.



###### Waiting on an object

 Um mecanismo de **sincronização** onde uma thread **pausa** sua execução até que uma **condição** seja atendida.  
- Diferente de `join()` (que espera uma thread terminar), `wait()` espera um **estado específico de um objeto**.  
- A thread **libera o lock** do objeto enquanto espera, permitindo que outras threads o modifiquem.  

Uso:

1. **Sincronização:**  
   - A thread deve obter o **lock do objeto** (usando `synchronized`) antes de chamar `wait()`.  
   ```java
   synchronized (objeto) {
       objeto.wait(); // Libera o lock e pausa
   }
   ```  

2. **Retorno da Espera:**  
   A thread acorda quando ocorre:  
   - **Timeout** (se usado `wait(ms)`).  
   - **Notificação** (`notify()` ou `notifyAll()`).  
   - **Interrupção** (`InterruptedException`).  

3. **Notificação:**  
   - Outra thread deve chamar `objeto.notify()` (para acordar **uma** thread) ou `notifyAll()` (para **todas**).  
   - A thread notificada **tenta readquirir o lock** antes de continuar.  


**Exemplo Prático**:  

**Cenário:** Uma thread lê um arquivo JAR e notifica outra thread quando o manifesto está pronto.  
```java
// Thread que espera o manifesto
ManifestFile m = new ManifestFile();
JarThread t = new JarThread(m, inputStream);

synchronized (m) {
    t.start();
    m.wait(); // Espera até ser notificada
    // Processa o manifesto...
}

// Thread que lê o JAR
public void run() {
    synchronized (theManifest) {
        // Lê o manifesto do stream...
        theManifest.notify(); // Notifica a thread esperando
    }
    // Lê o resto do arquivo...
}
```  


**Padrão Comum: Loop de Espera**:

- Como `wait()` pode acordar **espontaneamente** (sem notificação), sempre verifique a condição em um **loop**:  
```java
synchronized (lista) {
    while (lista.isEmpty()) { // Enquanto a condição não for atendida
        lista.wait(); // Libera o lock e espera
    }
    // Processa o recurso...
}
```  


**Exemplo:** Várias threads processando entradas de um log:  
```java
// Thread consumidora
synchronized (entries) {
    while (entries.isEmpty()) {
        entries.wait(); // Espera até ter entradas
    }
    String entry = entries.remove(0);
    // Processa a entrada...
}

// Thread produtora (adiciona entradas)
synchronized (entries) {
    entries.add(entry);
    entries.notifyAll(); // Acorda todas as threads esperando
}
```  


**Cuidados Importantes**:  
1. **Sempre sincronize no mesmo objeto** usado para `wait()`/`notify()`.  
2. **Prefira `notifyAll()`** quando múltiplas threads podem estar esperando.  
   - `notify()` seleciona **apenas uma** thread (aleatoriamente), podendo causar **starvation**.  
3. **Interrupção:**  
   - Se uma thread em `wait()` for interrompida (`interrupt()`), ela lança `InterruptedException` e deve tratar a saída adequadamente.  
4. **Condição de corrida:**  
   - Sem o loop, uma thread pode acordar **antes da condição ser verdadeira** (problema conhecido como *spurious wakeup*).  


**Alternativas Modernas**:
- **`java.util.concurrent`** oferece classes mais robustas para espera/notificação:  
  - **`Lock` + `Condition`**: Substitui `synchronized`/`wait()`/`notify()` com maior flexibilidade.  
  - **Filas bloqueantes** (`BlockingQueue`): Ideal para padrões produtor-consumidor.  


`wait()`/`notify()` são ferramentas poderosas para **coordenação entre threads**, mas exigem cuidado para evitar deadlocks e condições de corrida.  
- **Regras de ouro:**  
  1. **Sempre use em blocos `synchronized`**.  
  2. **Sempre espere em um loop** que verifica a condição.  
  3. **Prefira `notifyAll()` a `notify()`**.  
- Para cenários complexos, considere usar **`java.util.concurrent`** em vez de sincronização manual.


##### Thread Pools and Executors



 **Problema com Threads Individuais**  
- Criar e destruir threads manualmente tem **overhead significativo** (CPU/memória).  
- Muitas threads concorrentes podem **sobrecarregar o sistema**, especialmente em operações CPU-bound.  

**Solução: Thread Pools**  
- **Gerenciam um conjunto fixo de threads** que reutilizam tarefas, evitando custos de criação/destruição.  
- Controlam o **número máximo de threads ativas**, otimizando o uso de recursos.  

---

**Como Usar `Executors` em Java**  

A classe `java.util.concurrent.Executors` oferece fábricas para criar pools de threads:  

**1. Pool Fixo (`newFixedThreadPool`)**  
- Número fixo de threads.  
- Exemplo: Comprimir arquivos em paralelo (4 threads):  
  ```java
  ExecutorService pool = Executors.newFixedThreadPool(4);  
  for (File file : files) {  
      pool.submit(new GZipRunnable(file)); // Submete tarefas ao pool  
  }  
  pool.shutdown(); // Encerra após conclusão das tarefas  
  ```  

**2. Pool Dinâmico (`newCachedThreadPool`)**  
- Cria threads sob demanda e reutiliza threads ociosas.  
- Ideal para tarefas curtas e muitas.  

**3. Agendamento (`newScheduledThreadPool`)**  
- Executa tarefas periódicas ou com atraso.  


**Exemplo Prático: Compactação de Arquivos**  

**Classe `GZipRunnable` (Tarefa Individual)**  
```java
public class GZipRunnable implements Runnable {  
    private final File input;  

    public void run() {  
        if (!input.getName().endsWith(".gz")) {  
            File output = new File(input.getParent(), input.getName() + ".gz");  
            try (InputStream in = new BufferedInputStream(new FileInputStream(input));  
                 OutputStream out = new BufferedOutputStream(  
                     new GZIPOutputStream(new FileOutputStream(output)))) {  
                int b;  
                while ((b = in.read()) != -1) out.write(b);  
            } catch (IOException ex) {  
                System.err.println(ex);  
            }  
        }  
    }  
}  
```  

**Classe Principal (`GZipAllFiles`)**  

```java
public class GZipAllFiles {  
    public static void main(String[] args) {  
        ExecutorService pool = Executors.newFixedThreadPool(4);  
        for (String filename : args) {  
            File f = new File(filename);  
            if (f.isDirectory()) {  
                for (File file : f.listFiles()) {  
                    if (!file.isDirectory()) {  
                        pool.submit(new GZipRunnable(file));  
                    }  
                }  
            } else {  
                pool.submit(new GZipRunnable(f));  
            }  
        }  
        pool.shutdown(); // Não aceita novas tarefas, mas finaliza as existentes  
    }  
}  
```  


**Gerenciamento do Pool**:  
- **`shutdown()`**:  
  - Encerra o pool após conclusão das tarefas pendentes.  
- **`shutdownNow()`**:  
  - Interrompe tarefas em execução e descarta as pendentes.  
  - Útil em servidores que precisam parar abruptamente.  

 **Vantagens dos Thread Pools**:  
 
1. **Controle de recursos**: Limita threads ativas, evitando sobrecarga.  
2. **Reutilização**: Threads são reaproveitadas para múltiplas tarefas.  
3. **Escalabilidade**: Melhor desempenho em tarefas I/O-bound (ex.: redes, arquivos).  


**Alternativas Avançadas**: 
- **`Future` e `Callable`**: Para tarefas que retornam resultados ou podem falhar.  
  ```java
  Future<Result> future = pool.submit(new Callable<Result>() {  
      public Result call() throws Exception {  
          return processData();  
      }  
  });  
  Result r = future.get(); // Bloqueia até a conclusão  
  ```  
- **`ForkJoinPool`**: Para divisão de tarefas em subtarefas (ex.: algoritmos divide-and-conquer).  


**Use `Executors`** para gerenciar threads em aplicações de rede ou processamento paralelo.  
- **Prefira pools fixos** para operações CPU-bound e pools dinâmicos para I/O-bound.  
- **Evite criar threads manualmente** em loops — isso pode degradar o desempenho.  

> **Dica**: Em servidores de rede (ex.: HTTP), pools de threads são essenciais para balancear carga e concorrência.

