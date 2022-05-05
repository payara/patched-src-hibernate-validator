/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.parser;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenCollector.EL_DESIGNATOR;

/**
 * @author Hardy Ferentschik
 */
public class ELState implements ParserState {
	private static final Log log = LoggerFactory.make();

	@Override
	public void start(TokenCollector tokenCollector) {
		throw new IllegalStateException( "Parsing of message descriptor cannot start in this state" );
	}

	@Override
	public void terminate(TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( EL_DESIGNATOR );
		tokenCollector.terminateToken();
	}

	@Override
	public void handleNonMetaCharacter(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( EL_DESIGNATOR );
		tokenCollector.appendToToken( character );
		tokenCollector.terminateToken();
		tokenCollector.transitionState( new BeginState() );
		tokenCollector.next();
	}

	@Override
	public void handleBeginTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		tokenCollector.terminateToken();

		tokenCollector.appendToToken( EL_DESIGNATOR );
		tokenCollector.appendToToken( character );
		tokenCollector.makeELToken();
		tokenCollector.transitionState( new InterpolationTermState() );
		tokenCollector.next();
	}

	@Override
	public void handleEndTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		throw log.getUnbalancedBeginEndParameterException(
				tokenCollector.getOriginalMessageDescriptor(),
				character
		);
	}

	@Override
	public void handleEscapeCharacter(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( EL_DESIGNATOR );
		tokenCollector.appendToToken( character );
		// Do not go back to this state after the escape: $\ is not the start of an EL expression
		ParserState stateAfterEscape = new MessageState();
		tokenCollector.transitionState( new EscapedState( stateAfterEscape ) );
	}

	@Override
	public void handleELDesignator(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		handleNonMetaCharacter( character, tokenCollector );
	}
}


