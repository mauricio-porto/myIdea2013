# myIdea2013
 
 Este foi o primeiro projeto myIdea conduzido por Maurício Porto, Juliano Vacaro, João Ambrosi e Carlos Santos com o apoio da HP R&D Brazil em 2013.
 
## Aplicativo GuideDroid

O GuideDroid é o principal aplicativo do projeto, sendo idealizado por causa de uma necessidade identificada pelo Juliano, que é deficiente visual, como forma de auxiliar o deslocamento em ambientes novos e desconhecidos. Esse aplicativo pode ser visto como uma **"bengala eletrônica"**.

O nome GuideDroid significa "androide guia" e deriva de "Guide Dog", ou "cão guia" ([Guide Dog](http://en.wikipedia.org/wiki/Guide_dog)). 

## Objetivos

É um detector de proximidade.
Através da conexão bluetooth com uma placa Arduino que também é ligada a um sensor ultra-sônico, comunica ao usuário sempre que for detectado um obstáculo a uma distância menor do que um limite configurável.

## Projeto

### Classe BluetoothConnector

Esta classe faz o trabalho de ajuste e manutenção de conexões Bluetooth.

Ela possui uma thread que escuta por conexões entrantes, uma thread para manter uma conexão com um dispositivo e uma terceira thread para executar transmissão de dados quando conectada.

### Atividade BluetoothDeviceList

Esta atividade é mostrada como um diálogo. Ela lista todos os dispositivos pareados e dispositivos encontrados após uma busca.

Quando um dispositivo é escolhido pelo usuário, o endereço MAC é retornado à atividade que a disparou, através do Intent resultante.

### Classe Communicator

Esta calsse comunica de forma audível todas as notificações ao usuário, através da(s) forma(s) que o mesmo escolheu, incluindo voz ou outros avisos sonoros.

As notificações usuais em Android, sejam através da barra de notificações ou através de "Toasts", são feitas através deste serviço de forma audível.

### Serviço BluetoothReceiver

Este serviço é o ponto central do aplicativo, pois recebe as informações do sensor de proximidade através do Arduino, seleciona o que deve ser comunicado e requer tal comunicação ao serviço Communicator.


### Atividade GuideDroid

Esta atividade é a interface de configuração da aplicação. Por tratar-se de uma interface visual para usuários com deficiência visual, sua tela é baseada no estilo "dashboard", com ícones grandes.

Ela armazena todas as opções do usuário em SharedPreferences.

