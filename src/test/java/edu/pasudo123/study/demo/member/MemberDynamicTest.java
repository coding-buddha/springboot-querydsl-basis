package edu.pasudo123.study.demo.member;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import edu.pasudo123.study.demo.team.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static edu.pasudo123.study.demo.member.QMember.member;

@SpringBootTest
@DisplayName("Member 엔티티 테스트 클래스는")
@ActiveProfiles("test")
@Transactional
public class MemberDynamicTest {

    @Autowired
    private EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    @DisplayName("Member 엔티티를 선행으로 삽입한다.")
    public void init() {
        queryFactory = new JPAQueryFactory(em);

        // given
        em.persist(Member.builder()
                .username("PARK")
                .age(29)
                .team(Team.builder().name("NEW TEAM").build())
                .build());
        em.persist(Member.builder()
                .username("SON")
                .age(29)
                .team(Team.builder().name("YES TEAM").build())
                .build());
        em.persist(Member.builder()
                .username("CHA")
                .age(51)
                .team(Team.builder().name("NEW TEAM").build())
                .build());
        em.persist(Member.builder()
                .username("ABA")
                .age(51)
                .team(Team.builder().name("NEW TEAM").build())
                .build());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("Where 절 BooleanBuilder 를 이용한 테스트")
    public void dynamicQuerydslTest() {
        String username = "PARK";
        Integer age = 29;

        List<Member> members = searchMember(username, age);
    }

    private List<Member> searchMember(String username, Integer age) {
        return queryFactory
                .selectFrom(member)
                .where(whereClause(username, age))
                .fetch();
    }

    private BooleanBuilder whereClause(final String username, final Integer age) {

        /**
         * null 이 존재하면, where 조건 절에 포함 또는 미포함 여부를 결정한다.
         */

        final BooleanBuilder builder = new BooleanBuilder();

        if(username != null) {
            builder.and(member.username.eq(username));
        }

        if(age != null) {
            builder.and(member.age.eq(age));
        }

        return builder;
    }

    @Test
    @DisplayName("Where 다중 파라미터를 이용한 테스트")
    public void dynamicMultipleParamsTest() {
        String username = "PARK";
        Integer age = 29;

        List<Member> members = searchMemberAdvanced(username, age);
    }

    private List<Member> searchMemberAdvanced(String username, Integer age) {
        return queryFactory
                .selectFrom(member)
                // where(null) 이렇게 되면 해당 null 값은 where 절에서 무시가 된다.
                .where(usernameEq(username), ageEq(age))
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return (username == null)
                ? null
                : member.username.eq(username);
    }

    private BooleanExpression ageEq(Integer age) {
        if(age == null) {
            return null;
        }

        return member.age.eq(age);
    }

    public BooleanExpression allEq(final String username, final Integer age) {
        // dynamic 하게 조립이 가능하다.
        return usernameEq(username).and(ageEq(age));
    }
}
