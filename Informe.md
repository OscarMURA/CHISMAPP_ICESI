# **Informe de Implementación de una Sala de Chat en Java Utilizando Sockets TCP**

## **1. Introducción**

En la era digital actual, las aplicaciones de comunicación en tiempo real son fundamentales para facilitar la interacción entre usuarios. Este informe detalla la implementación de una sala de chat desarrollada en Java utilizando sockets TCP, permitiendo a múltiples clientes comunicarse simultáneamente. El proyecto se centra en la creación de un servidor robusto que gestiona conexiones de múltiples clientes, garantiza la unicidad de los nombres de usuario y facilita el intercambio de mensajes en tiempo real. Además, se aborda la implementación de funcionalidades avanzadas como llamadas de voz entre usuarios.

## **2. Objetivos**

- **Desarrollar una sala de chat en Java** que permita la comunicación en tiempo real entre múltiples clientes utilizando sockets TCP.
- **Implementar un servidor eficiente** que gestione conexiones concurrentes y asegure la unicidad de los nombres de usuario.
- **Facilitar la comunicación asíncrona** en el cliente, permitiendo la recepción de mensajes mientras se envían otros.
- **Incorporar funcionalidades adicionales** como llamadas de voz, manejo de grupos y registros históricos de comunicación.

## **3. Conceptos Básicos de TCP**

### **3.1. ¿Qué es TCP?**

El Protocolo de Control de Transmisión (TCP por sus siglas en inglés, Transmission Control Protocol) es uno de los protocolos fundamentales de la suite de protocolos de Internet (TCP/IP). TCP proporciona una comunicación confiable, orientada a la conexión y de flujo de datos entre aplicaciones que se ejecutan en dispositivos conectados a una red.

### **3.2. Características Principales de TCP**

- **Orientado a Conexión:** Antes de la transmisión de datos, se establece una conexión entre el cliente y el servidor mediante un proceso de handshake de tres vías.
- **Fiabilidad:** TCP garantiza la entrega de datos en el orden correcto, retransmitiendo paquetes perdidos y manejando la corrección de errores.
- **Control de Flujo:** Utiliza mecanismos como la ventana deslizante para gestionar la cantidad de datos que se pueden enviar sin recibir una confirmación.
- **Control de Congestión:** Ajusta dinámicamente la tasa de transmisión de datos para evitar la congestión de la red.

### **3.3. Comparación con Otros Protocolos**

A diferencia de UDP (Protocolo de Datagramas de Usuario), que es no orientado a la conexión y no garantiza la entrega de paquetes, TCP ofrece una comunicación más segura y confiable, siendo ideal para aplicaciones donde la integridad de los datos es crucial, como en una sala de chat.

## **4. Diseño de la Sala de Chat**

### **4.1. Arquitectura General**

La arquitectura de la sala de chat se basa en un modelo cliente-servidor:

- **Servidor de Chat:** Gestiona las conexiones de múltiples clientes, maneja la distribución de mensajes y coordina funcionalidades avanzadas como llamadas de voz y manejo de grupos.
- **Clientes de Chat:** Permiten a los usuarios conectarse al servidor, enviar y recibir mensajes, y utilizar funcionalidades adicionales.

### **4.2. Componentes Principales**

- **Servidor:**
  - **ChatServer:** Punto de entrada del servidor que escucha nuevas conexiones y asigna manejadores de clientes.
  - **ClientHandler:** Gestiona la comunicación individual con cada cliente conectado.
  - **GroupManager:** Maneja la creación y administración de grupos de chat.
  - **CallManager:** Gestiona las sesiones de llamadas entre usuarios.
  - **ServerDiscovery:** Facilita la detección y anuncio del servidor en la red.

- **Cliente:**
  - **ChatClient:** Punto de entrada del cliente que establece la conexión con el servidor y gestiona la interfaz de usuario.
  - **CallManager:** Gestiona las llamadas de voz entre usuarios.
  - **RecordAudio y RecordPlayer:** Manejan la grabación y reproducción de audio para las llamadas de voz.
  - **TCPConnection:** Maneja la comunicación TCP con el servidor.
  - **ClientDiscovery:** Descubre la ubicación del servidor en la red.
  - **HistorialRecorder:** Registra el historial de mensajes y actividades del usuario.

### **4.3. Flujo de Comunicación**

1. **Descubrimiento del Servidor:** El cliente utiliza `ClientDiscovery` para localizar el servidor mediante anuncios de broadcast.
2. **Establecimiento de Conexión:** Una vez descubierto, el cliente establece una conexión TCP con el servidor utilizando `TCPConnection`.
3. **Autenticación de Usuario:** El cliente envía su nombre de usuario al servidor para registrarse.
4. **Intercambio de Mensajes:** Los clientes pueden enviar mensajes al servidor, que los distribuye a los demás clientes conectados.
5. **Gestión de Grupos y Llamadas:** Los clientes pueden crear o unirse a grupos y realizar llamadas de voz utilizando `GroupManager` y `CallManager`.

## **5. Implementación en Java**

### **5.1. Entorno de Desarrollo**

La implementación se realizó en Java, aprovechando las capacidades de manejo de sockets y la concurrencia que ofrece el lenguaje. Se utilizaron varias librerías estándar de Java para la manipulación de audio, concurrencia y entrada/salida.

### **5.2. Descripción del Código**

#### **5.2.1. Servidor de Chat**

- **ChatServer.java:** Inicializa el servidor, escucha conexiones en un puerto asignado dinámicamente y utiliza un pool de hilos (`ExecutorService`) para manejar múltiples clientes simultáneamente. Además, inicia el `ServerDiscovery` para anunciar su presencia en la red.

- **ClientHandler.java:** Cada instancia de `ClientHandler` gestiona la comunicación con un cliente específico. Maneja la recepción de mensajes, comandos especiales (como crear grupos, mensajes directos, llamadas), y coordina con `GroupManager` y `CallManager` para distribuir los mensajes apropiadamente.

- **GroupManager.java:** Administra la creación y manejo de grupos de chat. Permite a los usuarios crear/join grupos y enviar mensajes a todos los miembros del grupo.

- **CallManager.java y CallSession.java:** Gestionan las sesiones de llamadas entre usuarios, manejando el estado de las llamadas (pendientes, activas) y asegurando que un usuario no participe en múltiples llamadas simultáneamente.

- **ServerDiscovery.java:** Envía anuncios periódicos de broadcast para que los clientes puedan descubrir el servidor automáticamente.

#### **5.2.2. Cliente de Chat**

- **ChatClient.java:** Punto de entrada del cliente que establece la conexión con el servidor, gestiona la interacción del usuario a través de la consola, y maneja los comandos de usuario como crear grupos, enviar mensajes, y gestionar llamadas.

- **TCPConnection.java:** Implementa una conexión TCP singleton para comunicarse con el servidor. Gestiona el envío y recepción de mensajes de manera asíncrona.

- **CallManager.java:** Similar al servidor, gestiona las llamadas de voz entre usuarios, incluyendo iniciar, aceptar, rechazar y finalizar llamadas.

- **RecordAudio.java y RecordPlayer.java:** Manejan la grabación y reproducción de audio para las llamadas de voz, utilizando las capacidades de captura y reproducción de audio de Java.

- **ClientDiscovery.java:** Busca y descubre el servidor de chat en la red mediante el manejo de anuncios de broadcast.

- **HistorialRecorder.java:** Registra el historial de mensajes y actividades del usuario, permitiendo generar informes históricos.

### **5.3. Manejo de Conexiones y Mensajes**

- **Gestión de Múltiples Conexiones (Servidor):** Utiliza `ExecutorService` con un pool de hilos para manejar múltiples `ClientHandler` simultáneamente. Cada conexión de cliente es gestionada en un hilo separado, permitiendo la concurrencia.

- **Distribución de Mensajes:** Cuando un cliente envía un mensaje, el `ClientHandler` correspondiente lo procesa y utiliza `GroupManager` o envía mensajes directos a otros `ClientHandler` según corresponda.

- **Unicidad de Nombres de Usuario:** Al registrar un usuario, el servidor almacena los nombres de usuario en un `ConcurrentHashMap`. Antes de aceptar un nuevo usuario, verifica que el nombre no esté ya en uso, evitando duplicados.

### **5.4. Manejo de Mensajes Asíncronos (Cliente)**

El cliente utiliza hilos separados para manejar la recepción y el envío de mensajes:

- **Hilo Principal:** Maneja la entrada del usuario y el envío de mensajes al servidor.
- **Hilo de Recepción (`TCPConnection`):** Escucha de manera continua los mensajes entrantes del servidor y los procesa sin bloquear la capacidad del usuario para enviar mensajes.

## **6. Respuesta a las Preguntas del Taller**

### **Parte 1**

#### **1. Preguntas con respecto al Servidor**

##### **a. ¿Cómo gestiona el servidor conexiones de múltiples clientes?**

El servidor gestiona conexiones de múltiples clientes utilizando un `ExecutorService` con un pool de hilos de tamaño fijo (`THREAD_POOL_SIZE = 10`). Cada vez que un nuevo cliente se conecta al servidor a través del `ServerSocket`, el servidor crea una nueva instancia de `ClientHandler` para gestionar la comunicación con ese cliente y lo asigna al pool de hilos para su ejecución concurrente.

**Referencias en el Código:**

```java
ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
// ...
ClientHandler clientHandler = new ClientHandler(clientSocket, groupManager, callManager);
pool.execute(clientHandler);
```

##### **b. ¿Cómo permite el servidor a los clientes enviar mensajes a la sala de chat y mostrarlos a todos los demás clientes conectados?**

El servidor utiliza `ClientHandler` para recibir mensajes de los clientes. Cuando un cliente envía un mensaje (por ejemplo, un mensaje de grupo o directo), el `ClientHandler` procesa el mensaje y, dependiendo de su tipo, utiliza `GroupManager` o directamente envía el mensaje a otros `ClientHandler` registrados en `userHandlers`. Esto asegura que todos los clientes conectados reciban los mensajes enviados.

**Referencias en el Código:**

```java
// En ClientHandler.java
else if (message.startsWith("/message")) {
    // ...
    groupManager.sendMessageToGroup(groupName, fullMessage);
}
// En GroupManager.java
public synchronized void sendMessageToGroup(String groupName, String message) {
    Set<ClientHandler> groupMembers = groups.get(groupName);
    if (groupMembers != null) {
        for (ClientHandler member : groupMembers) {
            member.sendMessage(message);
        }
    }
}
```

#### **2. Preguntas con respecto al Cliente**

##### **a. ¿Cómo interactúa el cliente con el servidor, qué tipo de socket usa?**

El cliente interactúa con el servidor utilizando un socket TCP (`java.net.Socket`). Utiliza la clase `TCPConnection` para establecer y gestionar la conexión con el servidor. El cliente se conecta al servidor mediante su dirección IP y puerto, y mantiene una comunicación bidireccional para enviar y recibir mensajes.

**Referencias en el Código:**

```java
// En ChatClient.java
clientConnection = TCPConnection.getInstance();
clientConnection.initAsClient(serverIp, serverPort);
```

```java
// En TCPConnection.java
public void initAsClient(String remoteIp, int remotePort) {
    try {
        this.socket = new Socket(remoteIp, remotePort);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

##### **b. ¿Cómo maneja el cliente mensajes entrantes de forma asíncrona mientras sigue siendo capaz de enviar mensajes?**

El cliente maneja los mensajes entrantes de forma asíncrona mediante la implementación de un hilo separado en la clase `TCPConnection`. Este hilo escucha continuamente los mensajes del servidor y los procesa a medida que llegan, permitiendo que el hilo principal del cliente continúe gestionando la entrada del usuario y el envío de mensajes sin bloqueos.

**Referencias en el Código:**

```java
// En TCPConnection.java
@Override
public void run() {
    try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            listener.onMessageReceived(line);
        }
    } catch (IOException e) {
       System.out.println("the server has been disconnected, Goodbye!");
       System.exit(0);
    }
}
```

```java
// En ChatClient.java
clientConnection.setListener(message -> {
    // Procesamiento de mensajes entrantes
});
clientConnection.start();
```

## **7.  Resultados**

- **Conexión de Múltiples Clientes:** Se conectaron múltiples instancias de clientes al servidor, verificando la capacidad del servidor para gestionar conexiones concurrentes sin fallos.
- **Envío y Recepción de Mensajes:** Se enviaron mensajes de texto y mensajes de voz entre los clientes, confirmando que los mensajes se distribuyen correctamente a todos los destinatarios.
- **Funciones de Llamada de Voz:** Se iniciaron y finalizaron llamadas de voz entre usuarios, comprobando la correcta transmisión y reproducción de audio.
- **Manejo de Grupos:** Se crearon y gestionaron grupos de chat, asegurando que los mensajes enviados a un grupo son recibidos únicamente por sus miembros.
- **Generación de Historiales:** Se generaron registros históricos de las comunicaciones, verificando la correcta captura y almacenamiento de los mensajes y actividades.

## **8. Conclusiones**

La implementación de una sala de chat en Java utilizando sockets TCP ha demostrado ser una solución eficaz para facilitar la comunicación en tiempo real entre múltiples usuarios. La arquitectura cliente-servidor desarrollada permite una gestión robusta de conexiones concurrentes, asegura la unicidad de los nombres de usuario y facilita el intercambio de mensajes de texto y voz. Además, la incorporación de funcionalidades avanzadas como el manejo de grupos y la gestión de llamadas de voz enriquece la experiencia del usuario.

A través de este proyecto, se han aplicado conceptos clave de redes y programación concurrente, consolidando habilidades en el manejo de sockets TCP, hilos y gestión de recursos en Java. Las pruebas realizadas confirmaron la eficacia y estabilidad del sistema, validando su capacidad para soportar escenarios de uso real.


# **Anexos**

## [**-  Diagrama de Clases**](https://github.com/OscarMURA/CHISMAPP_ICESI/blob/main/DIAGRAM%20CLASS%20UML%20-%20CHISMAPP_ICESI.pdf)



# **Referencias**

- Oracle Java Documentation. [https://docs.oracle.com/javase/8/docs/api/](https://docs.oracle.com/javase/8/docs/api/)
- RFC 793: Transmission Control Protocol. [https://tools.ietf.org/html/rfc793](https://tools.ietf.org/html/rfc793)
- "Effective Java" por Joshua Bloch.
- "Java Network Programming" por Elliotte Rusty Harold.

## Contacto

Si tiene alguna pregunta o comentario sobre el proyecto, podrá contactar a cualquiera de los integrantes del equipo a través de sus correos electrónicos.

- *Ricardo Andrés Chamorro Martínez* - [chamorroricardo29@gmail.com](mailto:chamorroricardo29@gmail.com)
- *Sebastián Erazo Ochoa* - [sbast6666@gmail.com](mailto:sbast6666@gmail.com)
- *Óscar Stiven Muñoz Ramírez* - [oscarmunozramirez01@gmail.com](mailto:oscarmunozramirez01@gmail.com)
- *Diego Armando Polanco Lozano* - [diegoarmandopolancolozano@gmail.com](mailto:diegoarmandopolancolozano@gmail.com)
- *Luis Manuel Rojas Correa* - [luis.manuel.rojas71@gmail.com](mailto:luis.manuel.rojas71@gmail.com)