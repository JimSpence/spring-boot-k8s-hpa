package com.learnk8s.app.repositories;

import com.learnk8s.app.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepository extends DataRepository {

    private static final Logger logger = LoggerFactory.getLogger(EventRepository.class);
    private static final String COLLECTION_NAME = "events";

    public void write(Event event) {
        mongoTemplate.insert(event, COLLECTION_NAME);
    }

    public long count(boolean state) {
        Query query = Query.query(Criteria.where("consumed").is(state));
        return mongoTemplate.count(query, COLLECTION_NAME);
    }

    public void update(Event event) {
        Query query = Query.query(Criteria.where("eventId").is(event.getEventId()));
        Update update = Update.update("eventId", (event.getEventId()))
                .set("name", event.getName())
                ;
        try {
            mongoTemplate.upsert(query, update, COLLECTION_NAME);
        } catch(Exception e) {
            write(event);
        }
    }

    public void updateOne() {
        Query query = Query.query(Criteria.where("consumed").is(false));
        Update update = Update.update("consumed", true);
        try {
            mongoTemplate.upsert(query, update, COLLECTION_NAME);
        } catch(Exception e) {
            logger.info("Record no longer available for update");
        }
    }

}
