package edu.yandex.project.cache.util;

import edu.yandex.project.domain.Item;

import java.util.List;

public record ItemPage(List<Item> items, Long total) {
}
