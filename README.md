# CHISMAPP_ICESI

## Integrantes

 - Ricardo Andres Chamorro Martinez
 - Sebastian Erazo Ochoa
 - Oscar Stiven Muñoz Ramirez
 - Diego Armando Polanco Lozano
 - Luis Manuel Rojas Correa

## Propósito del Programa

El objetivo de este programa es proporcionar una plataforma de chat en tiempo real que permite a múltiples usuarios conectarse, interactuar y comunicarse a través de mensajes de texto y voz, así como realizar llamadas. Esta aplicación está diseñada para ofrecer flexibilidad en las comunicaciones mediante funcionalidades como mensajes directos (privados), mensajes grupales, y la posibilidad de mantener un historial de conversaciones.

Además, la aplicación admite la creación de grupos de chat y la realización de llamadas de voz, todo gestionado mediante comandos simples y efectivos. Esta guía proporciona una descripción detallada de la ejecución del programa y el uso de los comandos disponibles.


## Ejecucion del programa

### Servidor

El servidor es el núcleo que gestiona todas las conexiones de los clientes y facilita la comunicación entre ellos.

Para iniciar el servidor, utiliza el siguiente comando:

    java -jar out/chatserver.jar

### Cliente

Cada cliente se conecta al servidor para interactuar con otros usuarios y participar en la sala de chat.

Para iniciar el cliente, ejecuta el siguiente comando:

    java -jar out/chatclient.jar
   
## Comandos de mensajeria

### Enviar mensaje privado

**Comando**: /dm username "message"
**Descripcion**:  Este comando permite enviar un mensaje directo a un usuario específico. Solo el destinatario verá el mensaje.
**Ejemplo**: `/dm andres hola`

### Enviar mensaje en grupos

**Comando**: /message group_name "message"
**Descripcion**:  Envío de mensajes a un grupo específico. Todos los miembros del grupo recibirán el mensaje.
**Ejemplo**: `/message icesi hola`

### Enviar mensaje de voz 

**Comando de inicio de grabacion**: /voice <username|group_name>
**Finalizar grabacion**: Presiona `enter` para finalizar la grabacion y enviar la nota de voz
**Descripcion**:  Envía un mensaje de voz a un usuario específico o a un grupo. La duración del mensaje está limitada por el tiempo de grabación antes de presionar
**Ejemplo usuario**: `/voice andres`
**Ejemplo grupo**: `/voice icesi`



## Comando de grupo

### Ingresar o Crear un Grupo

**Comando**: /group group_name
**Descripcion**:  Este comando sirve para crear un grupo o para que el usuario ingrese a un grupo especifico ya creado
**Ejemplo**: `/group icesi`

## Comandos de llamada

###  Inicio de una llamada

**Comando**: /call "username"
**Descripcion**:  Inicia una llamada de voz con un usuario específico. Solo el destinatario recibirá la solicitud de llamada.
**Ejemplo**: `/call andres`

### Finalizacion de una llamada

**Comando**: /endcall "username"
**Descripcion**:  Este comando sirve para finalizar una llamada en curso con un usuario especifico
**Ejemplo**: `/endcall andres`

## Comando de historial

**Comando**: /historical 
**Descripcion**:  Este comando genera un archivo de texto externo con el registro de todos los mensajes enviados y recibidos durante la sesión, incluido las llamadas. 
**Ejemplo**: `/historical`






