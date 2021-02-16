package cinema;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyInfoRepository extends CrudRepository<MyInfo, Long> {

    List<> findByReservationNo(Long reservationNo);
    List<> findByReservationNo(Long reservationNo);

}