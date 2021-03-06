package spring.project.nyangmong.domain.comment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
  @Query(value = "SELECT * FROM comment WHERE userId = :userId", nativeQuery = true)
  Comment findByuserId(@Param("userId") int userId);

  @Query(value = "SELECT * FROM comment", nativeQuery = true)
  Page<Comment> listComment(Pageable pq);

  @Query(value = "SELECT * FROM comment WHERE boardsId=:boardsId ORDER BY id DESC", nativeQuery = true)
  List<Comment> mFindByBoardsId(@Param("boardsId") Integer boardsId);

}