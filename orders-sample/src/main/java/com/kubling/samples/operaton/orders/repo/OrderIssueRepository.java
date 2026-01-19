package com.kubling.samples.operaton.orders.repo;

import com.kubling.samples.operaton.orders.model.entity.OrderIssue;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderIssueRepository extends CrudRepository<OrderIssue, Integer> {
}
