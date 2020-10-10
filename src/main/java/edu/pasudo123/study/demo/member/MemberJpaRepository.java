package edu.pasudo123.study.demo.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(final EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Member save(final Member member) {
        if(member.getId() == null) {
            em.persist(member);
            return member;
        } else {
            return em.merge(member);
        }
    }

    public void delete(Member member) {
        em.remove(member);
    }

    public Optional<Member> findById(final Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public List<Member> findAll() {
        return em.createQuery("SELECT m FROM member m", Member.class)
                .getResultList();
    }
}
