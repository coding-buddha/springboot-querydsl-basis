package edu.pasudo123.study.demo.team;

import edu.pasudo123.study.demo.member.Member;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Team")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    @ToString.Exclude
    List<Member> members = new ArrayList<>();

    @Builder
    public Team(final String name) {
        this.name = name;
    }
}
