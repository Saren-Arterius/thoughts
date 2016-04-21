package net.wtako.thoughts.interfaces;

public interface IUpdateThought {
    void updateScore(int id, int newScore);

    void deleteThought(int id);
}