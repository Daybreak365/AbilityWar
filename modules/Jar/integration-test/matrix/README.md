# 서버 버전별 공통 검사

`MatrixSmoke`는 버전별 NMS 클래스를 직접 참조하지 않고 AbilityWar의 공개 인터페이스를 호출합니다. 동일한 검사 JAR을 CraftBukkit, Spigot, Paper의 새 호환 모듈에 사용할 수 있습니다.

**별도의 테스트 월드에서만 실행하세요.** 엔티티와 블록을 생성하며 검사가 끝나면 서버를 종료합니다. 운영 서버에는 설치하지 마세요.

```sh
JAVA_HOME=/path/to/jdk bash modules/Jar/integration-test/matrix/build.sh \
  /path/to/bukkit-1.15.2-R0.1-SNAPSHOT.jar \
  modules/Jar/target/AbilityWar-26.0.0.jar
```

생성된 `modules/Jar/target/matrix-integration-test/MatrixSmoke.jar`와 검사 대상 AbilityWar JAR을 테스트 서버의 `plugins/`에 복사합니다. 해당 서버에 필요한 Java 버전으로 시작하고 `MATRIX SMOKE RESULT passed=... failed=0`을 확인합니다. 결과 줄이 없거나 기본 검사가 실패하면 성공으로 처리하지 않습니다. `-Dabilitywar.expectedAdapter=v1_18_R2`처럼 기대하는 어댑터를 명시할 수도 있습니다.

따옴표와 개행을 포함한 홀로그램 문구는 `MATRIX ROBUSTNESS RESULT`로 따로 보고합니다. 이전 공식 배포본의 일부 모듈에는 JSON 문자열을 직접 조합하는 문제가 있으므로, 일반적인 서버 호환 검사와 구분하여 원인과 실패 횟수를 기록합니다. 새 어댑터는 이 추가 검사도 실패가 없어야 합니다. 기존 모듈에서 추가 검사가 실패하면 전체 검사를 모두 통과했다고 표현하지 않습니다.

검사 범위: 플러그인 활성화, 버전 감지, 은둔자·광대·마그넷 등록, 사운드·파티클 매핑, 커스텀 머리, 홀로그램, 월드보더, 블록 레이 트레이스, 엔티티 조작, 다섯 종류의 피해, 화살 상태, 더미 생성·표시·제거, 더미 플레이어 대상 패킷 생성, 낚싯줄 정리입니다. `ChannelUtil`과 `InvisiblePlayer`가 있는 어댑터에서는 연결 접근과 은신 상태 복원도 확인합니다. 클라이언트 화면, 온라인 관전자의 패킷 필터 및 실제 플레이어의 조작은 검사하지 않습니다.

공식 Paper 서버 파일은 다음 도구로 받을 수 있습니다. 다운로드는 공식 API의 SHA256과 대조하며 `build.json`에 선택한 빌드를 보관합니다. Paper 아카이브에 없는 Minecraft 릴리스는 CraftBukkit 또는 Spigot으로 별도 검증해야 합니다.

```sh
python3 tools/paper-matrix.py --directory /path/to/test-bundles 1.18.2 1.20.6
python3 tools/paper-matrix.py --directory /path/to/test-bundles \
  --java /path/to/appropriate-jdk/bin/java 1.20.6
```

`--java`는 Paperclip 압축 해제만 실행합니다. 서버 실행이나 EULA 동의는 수행하지 않습니다. 버전마다 Java 요구사항이 다르므로 동일한 실행 파일로 모든 서버를 시작하지 마세요.
