from src.strategies.base import PythonStrategy

class MockSentimentStrategy(PythonStrategy):
    """
    Mock implementation of a Python Sentimemnt analysis strategy to prove
    the Java <-> Python gRPC pipeline.
    """

    @property
    def strategy_id(self) -> str:
        return "python-sentiment-mock"

    @property
    def version(self) -> str:
        return "1.0.0"

    @property
    def description(self) -> str:
        return "Basic mock sentiment analyzer (returns fixed positive sentiment)"

    def execute(self, input_data: dict, params: dict) -> tuple[dict, float]:
        # Return a mock output evaluating text as positive
        result = {
            "sentiment": "POSITIVE",
            "score": 0.95,
            "original_length": len(str(input_data))
        }
        return result, 0.95

