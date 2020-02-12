package headfront.dataexplorer.controlfx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextArea;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import java.util.Collection;
import java.util.Map;


public class AutoCompletionTextAreaBinding<T> extends AutoCompletionBinding<T> {

    /***************************************************************************
     *                                                                         *
     * Static properties and methods                                           *
     *                                                                         *
     **************************************************************************/

    private static <T> StringConverter<T> defaultStringConverter() {
        return new StringConverter<T>() {
            @Override
            public String toString(T t) {
                return t == null ? null : t.toString();
            }

            @SuppressWarnings("unchecked")
            @Override
            public T fromString(String string) {
                return (T) string;
            }
        };
    }

    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    /**
     * String converter to be used to convert suggestions to strings.
     */
    private StringConverter<T> converter;
    private Map<T, T> mappings;


    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new auto-completion binding between the given textField
     * and the given suggestion provider.
     *
     * @param textField
     * @param suggestionProvider
     */
    public AutoCompletionTextAreaBinding(final TextArea textField,
                                         Callback<ISuggestionRequest, Collection<T>> suggestionProvider,
                                         Map<T, T> mappings) {

        this(textField, suggestionProvider, AutoCompletionTextAreaBinding
                .<T>defaultStringConverter(), mappings);
    }

    /**
     * Creates a new auto-completion binding between the given textField
     * and the given suggestion provider.
     *
     * @param textField
     * @param suggestionProvider
     */
    public AutoCompletionTextAreaBinding(final TextArea textField,
                                         Callback<ISuggestionRequest, Collection<T>> suggestionProvider,
                                         final StringConverter<T> converter, Map<T, T> mappings) {

        super(textField, suggestionProvider, converter);
        this.converter = converter;
        this.mappings = mappings;

        getCompletionTarget().textProperty().addListener(textChangeListener);
        getCompletionTarget().focusedProperty().addListener(focusChangedListener);
    }


    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * {@inheritDoc}
     */
    @Override
    public TextArea getCompletionTarget() {
        return (TextArea) super.getCompletionTarget();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        getCompletionTarget().textProperty().removeListener(textChangeListener);
        getCompletionTarget().focusedProperty().removeListener(focusChangedListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void completeUserInput(T completion) {
        String newText = converter.toString(completion);
        T valueToUse = mappings.get(newText);
        TextArea textArea = getCompletionTarget();
        String oldText = textArea.getText();
        int i = oldText.trim().lastIndexOf(" ");
        if (i > -1) {
            String setText = oldText.substring(0, i + 1) + valueToUse;
            textArea.setText(setText);
        } else {
            textArea.setText(valueToUse.toString());
        }
        textArea.positionCaret(textArea.getText().length());
    }


    /***************************************************************************
     *                                                                         *
     * Event Listeners                                                         *
     *                                                                         *
     **************************************************************************/


    private final ChangeListener<String> textChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> obs, String oldText, String newText) {
            if (getCompletionTarget().isFocused()) {
                String[] split = newText.split(" ");
                if (split.length > 1) {
                    setUserInput(split[split.length - 1].replace("/", ""));
                } else {
                    setUserInput(newText.replace("/", ""));
                }
            }
        }
    };

    private final ChangeListener<Boolean> focusChangedListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> obs, Boolean oldFocused, Boolean newFocused) {
            if (newFocused == false)
                hidePopup();
        }
    };
}

