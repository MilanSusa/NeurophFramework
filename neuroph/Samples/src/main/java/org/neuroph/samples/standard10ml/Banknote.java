/**
 * Copyright 2013 Neuroph Project http://neuroph.sourceforge.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.neuroph.samples.standard10ml;

import java.util.Arrays;
import java.util.List;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.error.MeanSquaredError;
import org.neuroph.eval.ClassifierEvaluator;
import org.neuroph.eval.ErrorEvaluator;
import org.neuroph.eval.Evaluation;
import org.neuroph.eval.classification.ClassificationMetrics;
import org.neuroph.eval.classification.ConfusionMatrix;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.util.data.norm.MaxNormalizer;
import org.neuroph.util.data.norm.Normalizer;

/**
 *
 * @author Nevena Milenkovic
 */
/*
 INTRODUCTION TO THE PROBLEM AND DATA SET INFORMATION:

 1. Data set that will be used in this experiment: Banknote Dataset
    The Banknote Dataset involves predicting whether a given banknote is authentic given a number of measures taken from a photograph.
    The original data set that will be used in this experiment can be found at link:
    http://archive.ics.uci.edu/ml/machine-learning-databases/00267/data_banknote_authentication.txt

2. Reference:  University of Applied Sciences, Ostwestfalen-Lippe
   Owner of database: Volker Lohweg (University of Applied Sciences, Ostwestfalen-Lippe, volker.lohweg '@' hs-owl.de)
   Donor of database: Helene DÃ¶rksen (University of Applied Sciences, Ostwestfalen-Lippe, helene.doerksen '@' hs-owl.de)


3. Number of instances: 1 372

4. Number of Attributes: 4 pluss class attributes

5. Attribute Information:
 Inputs:
 5 attributes:
 4 continuous feature values are computed for each banknote:
 1) Variance of Wavelet Transformed image (continuous).
 2) Skewness of Wavelet Transformed image (continuous).
 3) Kurtosis of Wavelet Transformed image (continuous).
 4) Entropy of image (continuous).

 5) Output: Class variable (0 or 1). Value 0 indicates that a banknote is authentic.

6. Missing Values: None.




 */
public class Banknote implements LearningEventListener {

    public static void main(String[] args) {
        (new Banknote()).run();
    }

    public void run() {
        System.out.println("Creating data set...");
        String dataSetFile = "data_sets/ml10standard/databanknote.txt";
        int inputsCount = 4;
        int outputsCount = 1;

        // create training set from file
        DataSet dataSet = DataSet.createFromFile(dataSetFile, inputsCount, outputsCount, ",", false);
        DataSet[] trainTestSplit = dataSet.split(0.6, 0.4);
        DataSet trainingSet = trainTestSplit[0];
        DataSet testSet = trainTestSplit[1];

        Normalizer norm = new MaxNormalizer(trainingSet);
        norm.normalize(trainingSet);
        norm.normalize(testSet);

        System.out.println("Creating neural network...");
        MultiLayerPerceptron neuralNet = new MultiLayerPerceptron(TransferFunctionType.TANH, inputsCount, 1, outputsCount);

        neuralNet.setLearningRule(new MomentumBackpropagation());
        MomentumBackpropagation learningRule = (MomentumBackpropagation) neuralNet.getLearningRule();
        learningRule.addListener(this);

        // set learning rate and max error
        learningRule.setLearningRate(0.1);
        learningRule.setMaxError(0.01);
        System.out.println("Training network...");
        // train the network with training set
        neuralNet.learn(trainingSet);
        System.out.println("Training completed.");
        System.out.println("Testing network...");

        System.out.println("Network performance on the test set");
        evaluate(neuralNet, testSet);

        System.out.println("Saving network");
        // save neural network to file
        neuralNet.save("nn1.nnet");

        System.out.println("Done.");

        System.out.println();
        System.out.println("Network outputs for test set");
        testNeuralNetwork(neuralNet, testSet);
    }

    // Displays inputs, desired output (from dataset) and actual output (calculated by neural network) for every row from data set.
    public void testNeuralNetwork(NeuralNetwork neuralNet, DataSet testSet) {

        System.out.println("Showing inputs, desired output and neural network output for every row in test set.");

        for (DataSetRow testSetRow : testSet.getRows()) {
            neuralNet.setInput(testSetRow.getInput());
            neuralNet.calculate();
            double[] networkOutput = neuralNet.getOutput();

            System.out.println("Input: " + Arrays.toString(testSetRow.getInput()));
            System.out.println("Output: " + Arrays.toString(networkOutput));
            System.out.println("Desired output" + Arrays.toString(testSetRow.getDesiredOutput()));
        }
    }

    // Evaluates performance of neural network.
    // Contains calculation of Confusion matrix for classification tasks or Mean Ssquared Error and Mean Absolute Error for regression tasks.
    // Difference in binary and multi class classification are made when adding Evaluator (MultiClass or Binary).
    public void evaluate(NeuralNetwork neuralNet, DataSet dataSet) {

        System.out.println("Calculating performance indicators for neural network.");

        Evaluation evaluation = new Evaluation();
        evaluation.addEvaluator(new ErrorEvaluator(new MeanSquaredError()));

        evaluation.addEvaluator(new ClassifierEvaluator.Binary(0.5));
        evaluation.evaluate(neuralNet, dataSet);

        ClassifierEvaluator evaluator = evaluation.getEvaluator(ClassifierEvaluator.Binary.class);
        ConfusionMatrix confusionMatrix = evaluator.getResult();
        System.out.println("Confusion matrrix:\r\n");
        System.out.println(confusionMatrix.toString() + "\r\n\r\n");
        System.out.println("Classification metrics\r\n");
        ClassificationMetrics[] metrics = ClassificationMetrics.createFromMatrix(confusionMatrix);
        ClassificationMetrics.Stats average = ClassificationMetrics.average(metrics);
        for (ClassificationMetrics cm : metrics) {
            System.out.println(cm.toString() + "\r\n");
        }
        System.out.println(average.toString());
    }

    @Override
    public void handleLearningEvent(LearningEvent event) {
        MomentumBackpropagation bp = (MomentumBackpropagation) event.getSource();
        System.out.println(bp.getCurrentIteration() + ". iteration | Total network error: " + bp.getTotalNetworkError());
    }

}
