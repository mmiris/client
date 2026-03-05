import collections
from random import choice

Card = collections.namedtuple("Card", ["suit", "rank"])


class FrenchDeck:
    ranks = [str(n) for n in range(2, 11)] + list("JQKA")
    suits = "spades diamonds clubs hearts".split()

    def __init__(self) -> None:
        self._cards = [
            Card(suit, rank) for suit in self.suits for rank in self.ranks
        ]
        # from pprint import pprint
        # pprint(self._cards)

    def __len__(self):
        return len(self._cards)

    def __getitem__(self, position):
        return self._cards[position]


beer_card = Card('7', 'diamonds')
print(beer_card)

deck = FrenchDeck()
# print(deck[0::13])

for item in deck:
    print(item)

print("-" * 100)

for item in reversed(deck):
    print(item)

print(choice(deck))


print(Card(rank='spades', suit='7') in deck)


suit_values = dict(spades=3, hearts=2, diamonds=1, clubs=0)
def spades_high(card):
    rank_value = FrenchDeck.ranks.index(card.rank)
    return rank_value * len(suit_values) + suit_values[card.suit]

print(spades_high(Card(rank='2', suit='spades')))
