from abc import ABC, abstractmethod


class PythonStrategy(ABC):

    @property
    @abstractmethod
    def strategy_id(self) -> str:
        ...

    @property
    @abstractmethod
    def version(self) -> str:
        ...

    @property
    @abstractmethod
    def description(self) -> str:
        ...

    @abstractmethod
    def execute(self, input_data: dict, params: dict) -> tuple[dict, float]:
        """
        Executes the strategy.
        Returns a tuple: (output_dict, confidence_score)
        """
        ...

    def train(self, training_data: list[dict]) -> float:
        """
        Optional training method.
        Returns the new accuracy score.
        """
        return 0.0
