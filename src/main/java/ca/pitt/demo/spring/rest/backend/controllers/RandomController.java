package ca.pitt.demo.spring.rest.backend.controllers;

import java.util.Random;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ca.pitt.demo.spring.rest.backend.value.RandomValue;

@RestController
@RequestMapping(value = "/rand", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RandomController {

	/** Random number generator. */
	private Random randomGenerator = new Random();

	/**
	 * Get random number.
	 * 
	 * @return <code>RandomValue</code>
	 */
	@RequestMapping(method = RequestMethod.GET)
	public RandomValue getRandomValue() {
		RandomValue rand = new RandomValue(randomGenerator.nextInt());
		System.out.println("Random to send: " + rand.getValue());
		return rand;
	}
}
