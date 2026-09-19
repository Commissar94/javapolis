package javapolis.factory.model;

/** One input and one output resource; quantities and duration belong to the recipe. */
public final class Recipe {
    private final Resource input;
    private final int inputAmount;
    private final Resource output;
    private final int outputAmount;
    private final int duration;

    public Recipe(Resource input, int inputAmount, Resource output, int outputAmount, int duration) {
        if (input == null || output == null || inputAmount <= 0 || outputAmount <= 0 || duration <= 0) {
            throw new IllegalArgumentException("Рецепту нужны ресурсы и положительные количества и время");
        }
        this.input = input;
        this.inputAmount = inputAmount;
        this.output = output;
        this.outputAmount = outputAmount;
        this.duration = duration;
    }

    public Resource getInput() { return input; }
    public int getInputAmount() { return inputAmount; }
    public Resource getOutput() { return output; }
    public int getOutputAmount() { return outputAmount; }
    public int getDuration() { return duration; }
}
