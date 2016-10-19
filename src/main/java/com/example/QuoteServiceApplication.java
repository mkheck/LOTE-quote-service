package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
@EnableBinding(Sink.class)
public class QuoteServiceApplication {
    @Bean
    @Autowired
    CommandLineRunner commandLineRunner(QuoteRepository quoteRepository) {
        return args -> {
            quoteRepository.save(new Quote("The unexamined life is not worth living.", "Socrates"));
            quoteRepository.save(new Quote("What you do makes a difference, and you have to decide what kind of difference you want to make.", "Jane Goodall"));
            quoteRepository.save(new Quote("Do you want to know who you are? Don't ask. Act! Action will delineate and define you.", "Thomas Jefferson"));
            quoteRepository.save(new Quote("Love is the absence of judgment.", "Dalai Lama XIV"));
            quoteRepository.save(new Quote("You have power over your mind - not outside events. Realize this, and you will find strength.", "Marcus Aurelius, Meditations"));
            quoteRepository.save(new Quote("It's hard to beat a person who never gives up.", "Babe Ruth"));
            quoteRepository.save(new Quote("Imagination is the highest form of research.", "Albert Einstein"));

            quoteRepository.findAll().forEach(System.out::println);
        };
    }

	public static void main(String[] args) {
		SpringApplication.run(QuoteServiceApplication.class, args);
	}
}

@MessageEndpoint
class MessageProcessor {
    @Autowired
    QuoteRepository quoteRepository;

    @StreamListener(Sink.INPUT)
    public void saveQuote(Quote quote) {
        quoteRepository.save(quote);

        System.out.println(quote.toString());
    }
}

@RepositoryRestResource
interface QuoteRepository extends CrudRepository<Quote, Long> {
    @Query("select q from Quote q order by RAND()")
    List<Quote> getQuotesRandomOrder();
}

@RestController
class QuoteController {
    @Autowired
    QuoteRepository quoteRepository;

    @RequestMapping("/random")
    public Quote getRandomQuote() {
        return quoteRepository.getQuotesRandomOrder().get(0);
    }
}

@Entity
class Quote {
    @Id
    @GeneratedValue
    private Long id;
    private String text;
    private String source;

    public Quote() { // JPA & JSON
    }

    public Quote(String text, String source) {
        this.text = text;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}