import time

from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.common.keys import Keys

def scrapping_page():
    driver = webdriver.Chrome("chromedriver.exe")
    driver.get('https://tienda.mercadona.es/')
    time.sleep(2)
    inputElement = driver.find_element_by_name('postalCode')
    inputElement.send_keys('46006')
    time.sleep(2)
    inputElement.send_keys(Keys.ENTER)
    time.sleep(2)
    driver.find_element_by_xpath("//a[@href='/categories']").click()
    time.sleep(2)
    categories = driver.find_element_by_class_name('category-menu').find_elements_by_class_name('category-menu__item')
    for category in categories:
        category.click()
        time.sleep(3)
        category_name = category.find_element_by_tag_name('label').text
        subcategories = category.find_element_by_tag_name('ul').find_elements_by_tag_name('li')

        for subcategory in subcategories:
            subcategory_name = subcategory.find_element_by_tag_name('a').text
            subcategory.find_element_by_tag_name('a').click()
            time.sleep(3)

            products = driver.find_elements_by_class_name('product-cell')
            for product in products:
                product_info = product.find_elements_by_class_name('product-cell__content-link')
                name = product.find_element_by_class_name('product-cell__info').find_element_by_tag_name('h4').text
                model_aux = product.find_element_by_class_name('product-format').find_elements_by_tag_name('span')
                model = ''
                for aux in model_aux:
                    model += str(aux.text) + " "
                model = model.strip()
                price = product.find_element_by_class_name('product-cell__info').find_element_by_class_name('product-price').find_element_by_tag_name('p').text
                img = product.find_element_by_class_name('product-cell__image-wrapper').find_element_by_tag_name('img').get_attribute("src")
                product_mercadona = [name, model, price, category_name, subcategory_name, img]
                f = open("dump_data.txt", "a")
                f.write(str(product_mercadona) + "\n")
                f.close()


if __name__ == '__main__':
    scrapping_page()
