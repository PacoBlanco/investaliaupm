# Investalia - UPM #


Este proyecto está compuesto de:

> - Un microblogging sobre Android que conecta con la red social de Investalia.

> - Una simulación multi-agente de la red social de Investalia para probar el modelo de reputación.

Existiendo los siguientes subproyectos:

> - ==`InvestaliaAndroid`== se corresponde con el cliente android, para poder probarlo se  debe instalar el kit de desarrollo de Android (Android SDK) y opcionalmente el plugin de Android para Eclipse (ADT).

> - ==`InvestaliaServer`== contiene los agentes encargados de la gestión de los mensajes, para probarlo es necesario instalar WADE y MySQL.

> - ==`Simulador`== contiene la simulación multiagente.



## **Ejecución del microblogging:** ##

- Instalar la base de datos de MySQL: ejecutar el script de `path_to_InvestaliaServer/db`

- Configurar la conexión con la base de datos, indicando usuario, contraseña, ubicación y nombre de la base de datos, mediante los parámetros database\_user, database\_pass, database\_url y database\_name, en el fichero path\_to\_InvestaliaServer/cfg/db.properties

- Crear el siguiente archivo properties: path\_to\_wade/projects/investalia.properties para indicar la ruta en la que se ubica el proyecto de la siguiente forma:
> project-home=`/path_to_InvestaliaServer`

- Ejecutar WADE (con un terminal desde path\_to\_wade/):

> Desde Linux: ./startMain.sh investalia

> Desde Windows: startMain.bat investalia

- Ejecutar el cliente `InvestaliaAndroid.apk` desde un emulador de Android o desde Eclipse con el proyecto `InvestaliaAndroid`



## **Ejecución del Multi-agent Simulator:** ##

- Install Ascape project in your SO (http://ascape.sourceforge.net/)

- Install Ascape plugin for Eclipse: Install Ascape using the Eclipse Update Manager. Update site is http://ascape.sourceforge.net/eclipse. Please see the Eclipse user documentation for more details and post a support message if you have any difficulties. If this isn't working well for you for whatever reason then you can always Build Ascape directly in your workspace

- Launch Simulador.launch