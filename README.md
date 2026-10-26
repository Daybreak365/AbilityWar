# <img src="https://user-images.githubusercontent.com/39194432/96130737-475da400-0f33-11eb-9334-947ebd802aff.png">
<img src="https://img.shields.io/github/v/release/DayBreak365/AbilityWar?style=flat-square"></img>
<img src="https://img.shields.io/github/last-commit/DayBreak365/AbilityWar?style=flat-square"></img>
### 개발: [Daybreak](https://github.com/DayBreak365)
<br/>

**📋 License**

AbilityWar는 [능력자 전쟁 라이선스](https://github.com/DayBreak365/AbilityWar/blob/master/LICENSE.md)에 따라 라이선스가 부여됩니다.

**📎 Contribute**

새로운 도전, 무한한 가능성. 추가 개발은 모든 개발자에게 허용됩니다. 자신만의 Fork를 만들고 자신만의 Build를 만드세요.
모두에게 알리고 싶은 참신한 코드를 작성했다면 Pull Request를 통해 플러그인에 기여해주세요.

**📌 Versioning**

26.0.0부터 연도별 버저닝을 사용합니다. 버전의 첫 번째 숫자는 출시 연도의 끝 두 자리를 나타내며, 26.0.0의 26은 2026년을 의미합니다.

**🔧 Build**
- **Maven**
    - 배포용 통합 JAR은 `mvn -Dgpg.skip=true clean verify`로 빌드합니다. `modules/Jar/target/AbilityWar-26.0.0.jar`에 기존 지원 버전과 26.3 호환 모듈이 모두 포함됩니다.
    - AbilityWar를 로컬 Maven 레포지터리에 설치하려면 `mvn clean install` 명령을 실행하세요.
    - 과거 CraftBukkit 빌드 의존성을 준비하기 어려운 경우, 변경되지 않은 공식 배포본의 호환 모듈을 재사용하는 [통합 패키징 방법](tools/README.md)을 이용할 수 있습니다.
- **Minecraft 26.3**
    - Maven 실행은 JDK 21, 26.3 모듈 컴파일은 JDK 25 toolchain을 사용합니다. 통합 JAR의 서버 실행 Java 버전은 각 Minecraft 버전의 요구사항을 따릅니다. 26.3 서버는 Java 25가 필요합니다.
    - `mvn -P minecraft-26.3 -Dgpg.skip=true clean verify`는 개발 검사용 **26.3 전용** 파일 `AbilityWar-26.0.0-mc26.3.jar`를 생성합니다. 구버전 서버에는 배포용 통합 JAR을 사용하세요.
    - CraftBukkit 26.3 의존성은 공식 BuildTools로 먼저 설치해야 합니다. [설치 및 서버 검증 방법](modules/compatibility/v26_3/README.md)을 참고하세요.
    - 기본 빌드는 기존 버전 모듈과 26.3을 모두 포함합니다.
- **Craftbukkit Dependency**
    - AbilityWar 빌드시 다음 버전의 craftbukkit dependency가 필요합니다.
      - **1.12.2**, **1.13**, **1.13.2**, **1.14.4**, **1.15**, **1.16.1**, **1.16.2**, **1.16.5**, **1.17.1**, **1.19.2**, **1.19.3**, **1.20.4**, **26.3**
      - 최신 버전이 아닐 경우 **1.9.2**, **1.9.4**, **1.10.2**, **1.11.2**도 필요할 수 있음
    - [최신 버전의 BuildTools](https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar)를 내려받은 후 `java -jar BuildTools.jar --rev <version>` 명령을 이용하여 각 버전의 craftbukkit을 빌드한 후 local maven repository에 install 해주세요.
    - craftbukkit는 bukkit에 의존하므로 bukkit을 먼저 install 해야할 수 있습니다.
    - **1.19 ~ 1.20.4** 버전의 경우, CraftBukkit/pom.xml에서 specialsource plugin을 제거한 후 clean install 해주세요.

**🔗 Download**

1.12.2 버전 사용을 권장드립니다.

| 마인크래프트 버전 | 권장 플러그인 버전 |
|:----:|:----:|
| MC 26.3 | 26.0.0 이상 (서버 Java 25) |
| MC 1.20.4 | [최신 버전](https://github.com/DayBreak365/AbilityWar/releases/latest) |
| MC 1.20 ~ 1.20.3 | 지원 X |
| MC 1.19.x (Paper) | [최신 버전](https://github.com/DayBreak365/AbilityWar/releases/latest) |
| MC 1.17.x | [v3.0.0](https://github.com/DayBreak365/AbilityWar/releases/tag/20210912) |
| MC 1.12.x ~ 1.16.x | [최신 버전](https://github.com/DayBreak365/AbilityWar/releases/latest) |
| MC 1.9.x ~ 1.11.x | [v2.4.4](https://github.com/DayBreak365/AbilityWar/releases/tag/20210130) |
| MC 1.8.x | [v2.1.6.8-legacy](https://drive.google.com/uc?export=download&confirm=no_antivirus&id=1CYY7oH0IpEkH1vmL7--6fOfJ0skDgp3S) |
