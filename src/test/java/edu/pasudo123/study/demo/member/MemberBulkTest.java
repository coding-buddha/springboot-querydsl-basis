package edu.pasudo123.study.demo.member;

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

import static edu.pasudo123.study.demo.member.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Member 엔티티 테스트 클래스는")
@ActiveProfiles("test")
@Transactional
public class MemberBulkTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private MemberRepository memberRepository;

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
    @DisplayName("벌크 연산 테스트")
    public void bulkQuerydslTest() {

        /**
         * 벌크연산은 연속성 컨텍스트를 무시하고 바로 벌크연산이 나간다.
         */
        long count = queryFactory
                .update(member)
                .set(member.username, "NEW NAME")
                .where(member.age.gt(0))
                .execute();

        assertThat(count).isSameAs(4L);

        /**
         * 해당 부분의 테스트가 통과되는 이유는 @BeforeEach 에서
         * em.flush(), em.clear() 를 통해서 영속성 컨텍스트를 초기화 해주었기 때문이다.
         */

        // repeatable read 라고 부른다.
        Member foundMember = memberRepository.findById(1L).get();

        assertThat(foundMember.getUsername()).isEqualTo("NEW NAME");
    }

    @Test
    @DisplayName("벌크 연산 업데이트 테스트")
    public void bulkUpdateQuerydslTest() {

        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                // multiply, etc...
                .execute();
    }

    @Test
    @DisplayName("벌크 연산 삭제 테스트")
    public void bulkDeleteQuerydslTest() {

        long count = queryFactory
                .delete(member)
                .where(member.age.gt(0))
                .execute();

        assertThat(count).isSameAs(4L);
    }
}
