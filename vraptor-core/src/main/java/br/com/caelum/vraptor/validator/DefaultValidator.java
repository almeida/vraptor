
package br.com.caelum.vraptor.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.View;
import br.com.caelum.vraptor.ioc.RequestScoped;
import br.com.caelum.vraptor.util.test.MockResult;
import br.com.caelum.vraptor.view.ValidationViewsFactory;

/**
 * The default validator implementation.
 *
 * @author Guilherme Silveira
 */
@RequestScoped
public class DefaultValidator implements Validator {

    private final Result result;

	private final List<Message> errors = new ArrayList<Message>();
	private final ValidationViewsFactory viewsFactory;

    public DefaultValidator(Result result, ValidationViewsFactory factory) {
        this.result = result;
		this.viewsFactory = factory;
    }

    // TODO: do not use String consequences anymore
    // TODO: on error action should be defined by the onError method
    public void checking(Validations validations) {
        addAll(validations.getErrors());
    }


    public <T extends View> T onErrorUse(Class<T> view) {
    	if (!hasErrors()) {
    		return new MockResult().use(view); //ignore anything, no errors occurred
    	}
    	result.include("errors", errors);
    	return viewsFactory.instanceFor(view, errors);
    }

    public void addAll(Collection<? extends Message> message) {
		this.errors.addAll(message);
	}

    public void add(Message message) {
    	this.errors.add(message);
    }

	public boolean hasErrors() {
		return !errors.isEmpty();
	}
}
