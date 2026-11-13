# TravelNote

Diário de viagem para Android, desenvolvido em Java. O aplicativo permite registrar memórias com título, descrição, foto opcional, localização e pressão atmosférica medida pelo barômetro, além de pesquisar, editar e excluir notas e consultar os locais em um mapa.

## Tecnologias e requisitos

- Android Studio, Android Gradle Plugin 8.13.2 e Android SDK 35.
- Java 17.
- SQLite para guardar as notas no armazenamento privado do aplicativo.
- `LocationManager` e `Geocoder` para capturar coordenadas e tentar apresentar cidade e estado.
- Sensor Android `TYPE_PRESSURE` para registrar pressão atmosférica em hPa (mbar).
- `MemoryCardView`, uma view customizada desenhada com Canvas, usada nos cartões da lista.
- OpenStreetMap e Leaflet para mostrar as notas geolocalizadas; é necessária conexão com a internet.

O barômetro é opcional porque nem todo aparelho possui esse sensor. Quando disponível, a leitura aparece durante a criação da nota e é salva com ela. O app preserva a localização e a pressão originais ao editar uma nota. Sem barômetro ou sem uma leitura disponível, a nota continua podendo ser salva.

## Executar e compilar

Abra a pasta do projeto no Android Studio e execute a configuração `app`. No Windows, compile com `gradlew.bat assembleDebug`; em macOS/Linux, use `./gradlew assembleDebug`. O APK de depuração é gerado em `app/build/outputs/apk/debug/app-debug.apk`.

O projeto usa Android Gradle Plugin 8.13.2 e o wrapper Gradle 9.3.0. O workflow do GitHub Actions compila com Gradle 8.13, versão mínima exigida pelo AGP 8.13.

## Funcionalidades

- Criar notas com título, história e foto opcional.
- Registrar latitude e longitude; quando o serviço de geocodificação estiver disponível, exibir cidade e estado.
- Ler e guardar a pressão atmosférica em hPa durante a criação da nota.
- Pesquisar, editar e excluir notas sem substituir a localização ou pressão registradas originalmente.
- Abrir o mapa com marcadores das notas que possuem coordenadas.

## Organização principal

- `app/src/main/java/br/com/travelnote`: Activities, modelo, acesso ao SQLite, geocodificação e view customizada.
- `app/src/main/res/layout/activity_main.xml`: tela inicial, pesquisa e lista.
- `app/src/main/res/layout/item_memory_card.xml`: cartão customizado usado na lista do caderno.
- `app/src/main/res/layout/activity_editor.xml`: criação e edição de notas, localização e leitura do barômetro.
- `app/src/main/res/layout/activity_detalhes.xml`: conteúdo da nota, localização e pressão registrada, com componentes nativos.
- `app/src/main/res/layout/activity_map.xml`: tela do mapa.
- `MemoryDatabase`: banco `travelnote.db`, CRUD e migrações de esquema preservando as notas existentes.
- `PlaceResolver`: conversão de coordenadas em cidade e estado.
- `MapActivity`: mapa OpenStreetMap com marcadores.
- `.github/workflows/android.yml`: build automático em push, pull request ou execução manual; valida e publica o APK como artefato.

## Localização e permissões

A permissão de localização é solicitada ao criar uma nota. Se a permissão for negada ou o aparelho não fornecer uma posição, a nota pode ser salva sem coordenadas. A geocodificação e o mapa dependem de conexão com a internet.

## Banco de dados

O SQLite armazena título, descrição, URI opcional da foto, latitude, longitude, nome do local, data/hora e pressão atmosférica opcional. A migração mantém as notas existentes e acrescenta o valor do barômetro ao esquema.




## 📥 Download do TravelNote

<p align="center">
  <a href="https://github.com/GabrielLe0nel/TravelNote/releases/latest">
    <img src="https://img.shields.io/badge/VER%20VERS%C3%95ES-35D777?style=for-the-badge&logo=github&logoColor=black" alt="Ver versões">
  </a>
  &nbsp;
  <a href="https://github.com/GabrielLe0nel/TravelNote/releases/latest/download/app-debug.apk">
    <img src="https://img.shields.io/badge/BAIXAR%20APK-35D777?style=for-the-badge&logo=android&logoColor=black" alt="Baixar APK diretamente">
  </a>
</p>

<p align="center">
  Escolha entre consultar as versões disponíveis ou baixar diretamente o aplicativo.
</p>

