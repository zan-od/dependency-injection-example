package demo.disample.beans;

import demo.disample.annotations.Repository;

import java.util.List;

@Repository
public interface CrudRepository<ID, ENTITY> {

    ENTITY save(ENTITY obj) throws Exception;

    ENTITY getOne(ID id);

    void delete(ID id);

    List<ENTITY> listAll();

}
