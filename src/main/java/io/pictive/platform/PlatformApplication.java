package io.pictive.platform;

import io.pictive.platform.domain.collection.Collection;
import io.pictive.platform.domain.image.Image;
import io.pictive.platform.domain.user.User;
import io.pictive.platform.persistence.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.*;

@SpringBootApplication
public class PlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlatformApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(UserRepository userRepository) {

		return args -> {

			var john = User.withProperties("john@example.org");
			var jane = User.withProperties("jane@example.org");

			var displayNameTemplate = "Default collection of ";
			var defaultCollectionJohn = Collection.withProperties(displayNameTemplate + john.getMail());
			var defaultCollectionJane = Collection.withProperties(displayNameTemplate + jane.getMail());

			setUserCollectionReferences(john, defaultCollectionJohn);
			setUserCollectionReferences(jane, defaultCollectionJane);

			var image1 = Image.withProperties("some base64 payload");
			var image2 = Image.withProperties("some other base64 payload");
			var image3 = Image.withProperties("yet another base64 payload");

			defaultCollectionJohn.getImages().addAll(Set.of(image1, image2, image3));
			defaultCollectionJohn.getImages().forEach(i -> i.getContainedInCollections().add(defaultCollectionJohn));

			userRepository.saveAll(Arrays.asList(john, jane));

		};

	}

	private void setUserCollectionReferences(User user, Collection collection) {

		user.setDefaultCollection(collection);
		user.getSharedCollections().add(collection);
		user.getOwnedCollections().add(collection);

		collection.setOwner(user);
		collection.getSharedWith().add(user);

	}

}
