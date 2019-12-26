package daybreak.abilitywar.ability;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {

	/**
	 * 능력에 관련 있는 이벤트만 호출받습니다.
	 * <p>
	 * 이벤트가 ParticipantEvent일 경우 이벤트의 참가자가 능력을 소유하는 참가자와 일치하는지 확인
	 * 이벤트가 PlayerEvent일 경우 이벤트의 플레이어가 능력을 소유하는 플레이어와 일치하는지 확인
	 * 이벤트가 AbilityEvent일 경우 이벤트의 능력이 능력과 일치하는지 확인
	 */
	boolean onlyRelevant() default false;

}
