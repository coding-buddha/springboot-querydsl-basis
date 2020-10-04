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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Member 엔티티 테스트 클래스는")
@ActiveProfiles("test")
@Transactional
class MemberTest {

    @Autowired
    private EntityManager em;

    @BeforeEach
    @DisplayName("Member 엔티티를 선행으로 삽입한다.")
    public void init() {
        Member member = Member.builder()
                .username("PARK SUNG DONG")
                .age(29)
                .team(Team.builder().name("NEW TEAM").build())
                .build();

        em.persist(member);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("jpql 을 통해서 데이터를 조회한다.")
    public void jpqlTest() {

        final String query = "SELECT m FROM Member m WHERE m.username = :username";

        // when
        final Member foundMember = em.createQuery(query, Member.class)
                .setParameter("username", "PARK SUNG DONG")
                .getSingleResult();

        // then
        assertThat(foundMember.getUsername()).isEqualTo("PARK SUNG DONG");
    }

    @Test
    @DisplayName("querydsl 을 통해서 데이터를 조회한다.")
    public void querydslTest() {

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = new QMember("m");      // 임의로 `m` 을 삽입 (구분하는 이름인데 중요하진 않다고 함)


        final Member foundMember = queryFactory
                .selectFrom(member)
                .from(member)
                .where(member.username.eq("PARK SUNG DONG"))
                .fetchOne();

        assert foundMember != null;
        assertThat(foundMember.getUsername()).isEqualTo("PARK SUNG DONG");
    }

}