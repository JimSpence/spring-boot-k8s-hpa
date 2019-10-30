package com.learnk8s.app.routes;

import com.learnk8s.app.repositories.EventRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("consumerProcessor")
public class ConsumerProcessor implements Processor {

    @Autowired
    private EventRepository eventRepository;

    @Override
    public void process(Exchange exchange) throws Exception {
        if(eventRepository.count(false) > 0) {
            eventRepository.updateOne();
        }
    }
}
