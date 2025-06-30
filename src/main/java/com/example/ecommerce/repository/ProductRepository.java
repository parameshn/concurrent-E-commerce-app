package com.example.ecommerce.repository;

import com.example.ecommerce.entity.*;

import org.springframework.lang.NonNull;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    @NonNull
    Optional<Product> findById(@NonNull Long id);
    /*
     * When fetching this entity, treat it as optimistically locked — if someone
     * else modifies it before I save, fail my update to prevent data loss.
     */

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
    /*
     * “When fetching this entity, put a database-level write lock on the row so
     * that no other transaction can read or write it until I'm done
     */

    /*
     * You're updating critical, highly-contended data (e.g., inventory, balance)
     * 
     * You want strong isolation (e.g., financial systems)
     * 
     * You can't risk losing updates (e.g., concurrent stock purchases)
     */

     /*
      * You use @Query here because:
      * 
      * You want to apply a pessimistic lock
      * 
      * JPA does not allow locking on default findById() because it's already
      * implemented inside the proxy and cannot be overridden
      * 
      * So you define your own query to apply the lock
      * 
      * Pessimistic locking (like FOR UPDATE) must be done with a custom query
      * and @Lock, not with the built-in findById().
      */

    List<Product> findByCategory(String category);

    List<Product> findByStockQuantityLessThan(Integer quantity);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    List<Product> findAvailableProducts();
}

/*
 * JPA fetches the Product along with its version value.
 * 
 * When you later update and save() it:
 * 
 * JPA generates an UPDATE ... WHERE id = ? AND version = ?
 * 
 * If the row was modified (i.e., version changed), no rows will match ⇒ an
 * exception is thrown.
 * 
 * You expect high-read, low-write concurrency (e.g., a product page viewed a
 * lot, updated rarely)
 * 
 * You want to fail fast on conflicting writes
 * 
 * You want to ensure no lost updates during concurrent changes
 * 
 * Thread A loads product id=10, version=1
 * 
 * Thread B loads product id=10, version=1
 * 
 * Thread A updates stock and saves → version becomes 2
 * 
 * Thread B tries to save → version mismatch → OptimisticLockingFailureException
 * 
 * If you don’t have a @Version field, then:
 * @Lock(LockModeType.OPTIMISTIC)
 * is useless and has no effect — it's a waste in that context.
 */
/*
 * shouldn't lock everything — only the parts that:
 * Are updated concurrently by multiple threads/users
 * 
 * Could result in data loss, race conditions, or inconsistencies
 */

 /*
  * Feature findById() with @Lock(OPTIMISTIC) findById()
  * with @Lock(PESSIMISTIC_WRITE)
  * Supported? ✅ Yes ❌ No (must use custom method with @Query)
  * Why? Works with entity versioning at save-time Requires custom SQL (FOR
  * UPDATE), which default method does not expose
  * Solution Just override and annotate Use @Query + @Lock
  */